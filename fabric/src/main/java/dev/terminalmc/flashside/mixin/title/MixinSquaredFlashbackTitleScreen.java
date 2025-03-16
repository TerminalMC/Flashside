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

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.flashback.mixin.ui.MixinTitleScreen;
import com.moulberry.flashback.screen.FlashbackButton;
import dev.terminalmc.flashside.Flashside;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TitleScreen.class, priority = 1400)
public class MixinSquaredFlashbackTitleScreen {

    /**
     * Optionally repositions the Flashback title-screen icon button created by
     * {@link MixinTitleScreen#createOrPositionOpenSelectReplayScreenButton}.
     *
     * <p><b>Note:</b> MixinExtras errors are expected on the annotation,
     * method and target.</p>
     */
    @SuppressWarnings("JavadocReference")
    @TargetHandler(
            mixin = "com.moulberry.flashback.mixin.ui.MixinTitleScreen",
            name = "createOrPositionOpenSelectReplayScreenButton"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "NEW",
                    target = "(IIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lcom/moulberry/flashback/screen/FlashbackButton;"
            )
    )
    private FlashbackButton wrapConstructFlashbackButton(int x, int y, int width, int height,
                                                         Component component, Button.OnPress onPress,
                                                         Operation<FlashbackButton> original) {
        if (Flashside.fbTitleScreenX != -1) x = Flashside.fbTitleScreenX;
        if (Flashside.fbTitleScreenY != -1) y = Flashside.fbTitleScreenY;
        return original.call(x, y, width, height, component, onPress);
    }
}
