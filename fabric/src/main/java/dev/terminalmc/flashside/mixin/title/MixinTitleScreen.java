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

package dev.terminalmc.flashside.mixin.title;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.terminalmc.flashside.Flashside;
import dev.terminalmc.flashside.config.Config;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    /**
     * Clears the stored button positions from the previous menu creation.
     */
    @Inject(
            method = "init",
            at = @At("HEAD")
    )
    private void onCreateNormalMenuOptions(CallbackInfo ci) {
        Flashside.fbTitleScreenX = -1;
        Flashside.fbTitleScreenY = -1;
        Flashside.mmTitleScreenY = -1;
        Flashside.mmTitleScreenIcon = false;
    }

    /**
     * Calculates the positions for the Flashback and ModMenu buttons.
     */
    @WrapOperation(
            method = "createNormalMenuOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
                    ordinal = 0
            )
    )
    private GuiEventListener wrapAddRenderableWidget(TitleScreen instance, 
                                                     GuiEventListener guiEventListener, 
                                                     Operation<GuiEventListener> original, 
                                                     @Local(ordinal = 1, argsOnly = true) int rowHeight) {
        if (guiEventListener instanceof AbstractWidget widget && Config.options().editTitleScreen) {
            // Flashback button position
            if (Config.options().leftSide) Flashside.fbTitleScreenX = widget.getX() - widget.getHeight() - 4;
            Flashside.fbTitleScreenY = widget.getY() + rowHeight * Config.options().startRowTitleScreen;
            // ModMenu button position
            if (!Config.options().leftSide) Flashside.mmTitleScreenY = Flashside.fbTitleScreenY + rowHeight;
        }
        return original.call(instance, guiEventListener);
    }
}
