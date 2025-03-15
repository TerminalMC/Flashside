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

package dev.terminalmc.flashside.gui.widget;

import com.mojang.datafixers.util.Pair;
import com.moulberry.flashback.screen.FlashbackButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FlashsideButton extends FlashbackButton {
    public static final ResourceLocation OVERLAY_START = ResourceLocation.parse("flashside:textures/overlay_start.png");
    public static final ResourceLocation OVERLAY_FINISH = ResourceLocation.parse("flashside:textures/overlay_finish.png");
    public static final ResourceLocation OVERLAY_PAUSE = ResourceLocation.parse("flashside:textures/overlay_pause.png");
    public static final ResourceLocation OVERLAY_UNPAUSE = ResourceLocation.parse("flashside:textures/overlay_unpause.png");
    public static final ResourceLocation OVERLAY_CANCEL = ResourceLocation.parse("flashside:textures/overlay_cancel.png");
    public static final ResourceLocation OVERLAY_UNKNOWN = ResourceLocation.parse("flashside:textures/overlay_unknown.png");
    
    private final ResourceLocation overlay;
    
    public FlashsideButton(int x, int y, int width, int height, Component component,
                           OnPress onPress, Pair<ResourceLocation,String> info) {
        super(x, y, width, height, component, onPress);
        this.overlay = info.getFirst();
        this.setTooltip(Tooltip.create(Component.literal(info.getSecond())));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int i, int j, float f) {
        super.renderWidget(graphics, i, j, f);

        final int size = 16;
        int paddingX = (this.getWidth() - size) / 2;
        int paddingY = (this.getHeight() - size) / 2;

        int x = this.getX() + paddingX;
        int y = this.getY() + paddingY;

        graphics.blit(overlay, x, y, 0f, 0f, size, size, size, size);
    }
}
