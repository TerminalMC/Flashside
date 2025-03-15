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
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import dev.terminalmc.flashside.Flashside;
import dev.terminalmc.flashside.config.Config;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModMenuEventHandler.class)
public class MixinModMenuEventHandler {

    /**
     * Optionally repositions the ModMenu title-screen icon button.
     */
    @WrapOperation(
            method = "afterTitleScreenInit",
            at = @At(
                    value = "NEW",
                    target = "(IIIIIIILnet/minecraft/resources/ResourceLocation;IILnet/minecraft/client/gui/components/Button$OnPress;Lnet/minecraft/network/chat/Component;)Lcom/terraformersmc/modmenu/gui/widget/UpdateCheckerTexturedButtonWidget;"
            )
    )
    private static UpdateCheckerTexturedButtonWidget wrapConstructUpdateCheckerTexturedButtonWidget(
            int x, int y, int width, int height, int u, int v, int hoveredVOffset,
            ResourceLocation texture, int textureWidth, int textureHeight,
            Button.OnPress pressAction, Component message,
            Operation<UpdateCheckerTexturedButtonWidget> original) {
        if (Flashside.mmTitleScreenY != -1) {
            if (Config.options().modmenuIconTop) {
                int temp = Flashside.fbTitleScreenY;
                Flashside.fbTitleScreenY = Flashside.mmTitleScreenY;
                Flashside.mmTitleScreenY = temp;
            }
            y = Flashside.mmTitleScreenY;
        }
        return original.call(x, y, width, height, u, v, hoveredVOffset, texture,
                textureWidth, textureHeight, pressAction, message);
    }
}
