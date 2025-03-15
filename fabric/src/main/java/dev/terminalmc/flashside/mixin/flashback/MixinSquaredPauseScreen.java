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

package dev.terminalmc.flashside.mixin.flashback;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.flashback.screen.BottomTextWidget;
import dev.terminalmc.flashside.Flashside;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PauseScreen.class, priority = 1100)
public class MixinSquaredPauseScreen {

    /**
     * Intercept the Flashback buttons.
     */
    @TargetHandler(
            mixin = "com.moulberry.flashback.mixin.ui.MixinPauseScreen",
            name = "createPauseMenu"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"
            )
    )
    private <T extends LayoutElement> T wrapAddChild1(GridLayout.RowHelper instance, T child, Operation<T> original) {
        if (cancelAdd(child)) return null;
        else return original.call(instance, child);
    }

    /**
     * Intercept the Flashback buttons.
     */
    @TargetHandler(
            mixin = "com.moulberry.flashback.mixin.ui.MixinPauseScreen",
            name = "createPauseMenu"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"
            )
    )
    private <T extends LayoutElement> T wrapAddChild2(GridLayout.RowHelper instance, T child, int occupiedColumns, Operation<T> original) {
        if (cancelAdd(child)) return null;
        else return original.call(instance, child, occupiedColumns);
    }
    
    @Unique
    private boolean cancelAdd(LayoutElement element) {
        boolean cancel = false;
        if (element instanceof Button button) {
            if (button.getMessage().getContents() instanceof 
                    PlainTextContents.LiteralContents contents) {
                if (Flashside.firstStrings.contains(contents.text())) {
                    Flashside.fbButton1 = button;
                    cancel = true;
                } else if (Flashside.secondStrings.contains(contents.text())) {
                    Flashside.fbButton2 = button;
                    cancel = true;
                } else if (Flashside.thirdStrings.contains(contents.text())) {
                    Flashside.fbButton3 = button;
                    cancel = true;
                }
            }
        } else if (element instanceof BottomTextWidget) {
            cancel = true;
        }
        return cancel;
    }
}
