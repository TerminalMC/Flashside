/*
 * Copyright 2025 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.terminalmc.flashside.mixin.pause;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.moulberry.flashback.Flashback;
import dev.terminalmc.flashside.Flashside;
import dev.terminalmc.flashside.config.Config;
import dev.terminalmc.flashside.gui.widget.FlashsideButton;
import dev.terminalmc.flashside.mixin.accessor.ButtonAccessor;
import dev.terminalmc.flashside.mixin.accessor.GridLayoutAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

import static dev.terminalmc.flashside.config.Config.options;

@Mixin(PauseScreen.class)
public class MixinPauseScreen extends Screen {
    @Shadow
    @Final
    private static int BUTTON_PADDING;

    protected MixinPauseScreen(Component title) {
        super(title);
    }

    /**
     * Clears the stored buttons from the previous menu creation.
     */
    @Inject(
            method = "createPauseMenu",
            at = @At("HEAD")
    )
    private void beforeCreatePauseMenu(CallbackInfo ci) {
        Arrays.fill(Flashside.fbButtons, null);
        Flashside.mmButton = null;
    }

    /**
     * Adds the stored buttons to the pause screen.
     */
    @Inject(
            method = "createPauseMenu",
            at = @At("TAIL")
    )
    private void afterCreatePauseMenu(CallbackInfo ci, @Local GridLayout grid) {
        // Get the first button of the pause menu - we assume this is a
        // full-width button at the top
        Button ref = null;
        for (LayoutElement element : ((GridLayoutAccessor) grid).getChildren()) {
            if (element instanceof Button button) {
                ref = button;
                break;
            }
        }
        // Should never happen but we check it anyway
        if (ref == null) return;

        // Get reference values
        int smallButtonSize = ref.getHeight();
        int rowHeight = ref.getHeight() + BUTTON_PADDING;

        // Calculate position for first side button
        int x = options().leftSide
                ? ref.getX() - smallButtonSize - BUTTON_PADDING
                : ref.getRight() + BUTTON_PADDING;
        int y = ref.getY() + (rowHeight * options().startRow);

        // If configured to place ModMenu button on top, add it
        boolean mmButtonPlaced = false;
        if (Flashside.mmButton != null && options().modmenuIconTop) {
            Flashside.mmButton.setX(x);
            Flashside.mmButton.setY(y);
            this.addRenderableWidget(Flashside.mmButton);
            y += rowHeight;
            mmButtonPlaced = true;
        }

        // Store the command button if it's enabled and we're recording (even if paused)
        if (options().commandEnabled && Flashback.RECORDER != null) {
            Flashside.storeButton(new FlashsideButton(0, 0, smallButtonSize, smallButtonSize,
                            Component.empty(), (b) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.getConnection() != null) {
                    mc.getConnection().sendCommand(
                            options().commandString.substring(1));
                    mc.setScreen(null);
                }}, new Pair<>(FlashsideButton.OVERLAY_COMMAND, options().commandString)),
                    Config.Action.COMMAND);
        }

        // Add all stored Flashside buttons
        for (Button b : Flashside.fbButtons) {
            if (b != null) {
                if (b instanceof FlashsideButton) {
                    b.setPosition(x, y);
                    this.addRenderableWidget(b);
                } else {
                    this.addRenderableWidget(getFlashsideButton(x, y, smallButtonSize, b));
                }
                y += rowHeight;
            }
        }

        // If ModMenu button hasn't been added, add it
        if (Flashside.mmButton != null && !mmButtonPlaced) {
            Flashside.mmButton.setX(x);
            Flashside.mmButton.setY(y);
            this.addRenderableWidget(Flashside.mmButton);
        }
    }

    /**
     * @return the {@link FlashsideButton} corresponding to the specified
     * Flashback {@link Button}.
     */
    @Unique
    private static @NotNull FlashsideButton getFlashsideButton(int x, int y, int size, Button original) {
        return new FlashsideButton(x, y, size, size, Component.empty(),
                ((ButtonAccessor) original).getOnPress(), getButtonInfo(original.getMessage().getString())
        );
    }

    /**
     * @return the icon {@link ResourceLocation} corresponding to the specified
     * button message.
     *
     * <p><b>Note:</b> Flashback buttons are not currently translatable, so the
     * strings are constant literals.</p>
     */
    @Unique
    private static Pair<ResourceLocation, String> getButtonInfo(String text) {
        return switch(text) {
            case Flashside.startString -> new Pair<>(FlashsideButton.OVERLAY_START, text);
            case Flashside.finishString -> new Pair<>(FlashsideButton.OVERLAY_FINISH, text);
            case Flashside.pauseString -> new Pair<>(FlashsideButton.OVERLAY_PAUSE, text);
            case Flashside.unpauseString -> new Pair<>(FlashsideButton.OVERLAY_UNPAUSE, text);
            case Flashside.cancelString -> new Pair<>(FlashsideButton.OVERLAY_CANCEL, text);
            default -> new Pair<>(FlashsideButton.OVERLAY_UNKNOWN, "Unknown Function");
        };
    }
}
