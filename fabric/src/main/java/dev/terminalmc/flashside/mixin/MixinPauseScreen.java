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

package dev.terminalmc.flashside.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import dev.terminalmc.flashside.Flashside;
import dev.terminalmc.flashside.config.Config;
import dev.terminalmc.flashside.gui.widget.FlashsideButton;
import dev.terminalmc.flashside.mixin.accessor.ButtonAccessor;
import dev.terminalmc.flashside.mixin.accessor.GridLayoutAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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

import java.util.function.Consumer;

@Mixin(PauseScreen.class)
public class MixinPauseScreen extends Screen {
    @Shadow
    @Final
    private static int BUTTON_PADDING;

    protected MixinPauseScreen(Component title) {
        super(title);
    }
    
    @Inject(
            method = "createPauseMenu",
            at = @At("HEAD")
    )
    private void onCreatePauseMenu(CallbackInfo ci) {
        Flashside.fbButton1 = null;
        Flashside.fbButton2 = null;
        Flashside.fbButton3 = null;
        Flashside.mmButton = null;
    }
    
    @WrapOperation(
            method = "createPauseMenu",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout;visitWidgets(Ljava/util/function/Consumer;)V"
            )
    )
    private void wrapVisitWidgets(GridLayout grid, Consumer<AbstractWidget> consumer, 
                                  Operation<Void> original) {
        if (grid != null) {
            Button referenceButton = null;
            for (LayoutElement element : ((GridLayoutAccessor) grid).getChildren()) {
                if (element instanceof Button button) {
                    referenceButton = button;
                    break;
                }
            }
            if (referenceButton != null) {
                int bHeight = referenceButton.getHeight();
                int rowHeight = bHeight + BUTTON_PADDING;

                int x = Config.options().leftSide 
                        ? referenceButton.getX() - referenceButton.getHeight() - 4
                        : referenceButton.getRight() + 4;
                int y = referenceButton.getY() + (rowHeight * Config.options().startRow);
                
                boolean mmButtonPlaced = false;
                if (Flashside.mmButton != null && Config.options().modmenuIconTop) {
                    Flashside.mmButton.setX(x);
                    Flashside.mmButton.setY(y);
                    this.addRenderableWidget(Flashside.mmButton);
                    y += rowHeight;
                    mmButtonPlaced = true;
                }

                if (Flashside.fbButton1 != null) {
                    this.addRenderableWidget(getFlashsideButton(x, y, bHeight, Flashside.fbButton1));
                    y += rowHeight;
                    if (Flashside.fbButton2 != null) {
                        this.addRenderableWidget(getFlashsideButton(x, y, bHeight, Flashside.fbButton2));
                        y += rowHeight;
                        if (Flashside.fbButton3 != null) {
                            this.addRenderableWidget(getFlashsideButton(x, y, bHeight, Flashside.fbButton3));
                            y += rowHeight;
                        }
                    }
                }
                
                if (Flashside.mmButton != null && !mmButtonPlaced) {
                    Flashside.mmButton.setX(x);
                    Flashside.mmButton.setY(y);
                    this.addRenderableWidget(Flashside.mmButton);
                }
            }
        }
        
        original.call(grid, consumer);
    }

    @Unique
    private static @NotNull FlashsideButton getFlashsideButton(int x, int y, int height, Button original) {
        FlashsideButton fsb = new FlashsideButton(x, y, height, height, Component.empty(),
                ((ButtonAccessor) original).getOnPress(), getButtonInfo(original.getMessage().getString())
        );
        fsb.setTooltip(Tooltip.create(original.getMessage()));
        return fsb;
    }
    
    @Unique
    private static Pair<ResourceLocation,String> getButtonInfo(String text) {
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
