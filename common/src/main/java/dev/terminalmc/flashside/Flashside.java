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

package dev.terminalmc.flashside;

import dev.terminalmc.flashside.config.Config;
import dev.terminalmc.flashside.util.ModLogger;
import net.minecraft.client.gui.components.Button;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Flashside {
    public static final String MOD_ID = "flashside";
    public static final String MOD_NAME = "Flashside";
    public static final ModLogger LOG = new ModLogger(MOD_NAME);
    
    public static final String startString = "Start Recording";
    public static final String finishString = "Finish Recording";
    public static final String pauseString = "Pause Recording";
    public static final String unpauseString = "Unpause Recording";
    public static final String cancelString = "Cancel Recording";
    
    public static final List<String> firstStrings = List.of(startString, finishString);
    public static final List<String> secondStrings = List.of(pauseString, unpauseString);
    public static final List<String> thirdStrings = List.of(cancelString);
    
    public static @Nullable Button fbButton1 = null;
    public static @Nullable Button fbButton2 = null;
    public static @Nullable Button fbButton3 = null;
    public static @Nullable Button mmButton = null;
    
    public static int fbTitleScreenX = -1;
    public static int fbTitleScreenY = -1;
    public static int mmTitleScreenY = -1;
    public static boolean mmTitleScreenIcon = false;

    public static void init() {
        Config.getAndSave();
    }

    public static void onConfigSaved(Config instance) {
    }
}
