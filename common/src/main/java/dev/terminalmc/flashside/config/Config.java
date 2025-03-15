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

package dev.terminalmc.flashside.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.terminalmc.flashside.Flashside;
import dev.terminalmc.flashside.platform.Services;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

public class Config {
    private static final Path CONFIG_DIR = Services.PLATFORM.getConfigDir();
    private static final String FILE_NAME = Flashside.MOD_ID + ".json";
    private static final String BACKUP_FILE_NAME = Flashside.MOD_ID + ".unreadable.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Options

    public final Options options = new Options();

    public static Options options() {
        return Config.get().options;
    }

    public static class Options {
        // Common

        public static final boolean leftSideDefault = false;
        public boolean leftSide = leftSideDefault;

        public static final boolean modmenuIconTopDefault = false;
        public boolean modmenuIconTop = modmenuIconTopDefault;
        
        // Pause menu options
        
        public static final int startRowDefault = 1;
        public int startRow = startRowDefault;

        public static final boolean commandEnabledDefault = false;
        public boolean commandEnabled = commandEnabledDefault;

        public static final String commandStringDefault = "/flashback mark";
        public String commandString = commandStringDefault;
        
        public static final Supplier<Action[]> actionsDefault = () -> new Action[]{
                Action.START_STOP,
                Action.PAUSE_UNPAUSE,
                Action.CANCEL,
                Action.COMMAND
        };
        public Action[] actions = actionsDefault.get();

        // Title screen options

        public static final boolean editTitleScreenDefault = false;
        public boolean editTitleScreen = editTitleScreenDefault;

        public static final int startRowTitleScreenDefault = 1;
        public int startRowTitleScreen = startRowTitleScreenDefault;
    }

    public enum Action {
        START_STOP,
        PAUSE_UNPAUSE,
        CANCEL,
        COMMAND
    }

    // Instance management

    private static Config instance = null;

    public static Config get() {
        if (instance == null) {
            instance = Config.load();
        }
        return instance;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Config getAndSave() {
        get();
        save();
        return instance;
    }

    @SuppressWarnings("unused")
    public static Config resetAndSave() {
        instance = new Config();
        save();
        return instance;
    }

    // Validation

    private void validate() {
        if (options.commandString == null || options.commandString.isBlank()) {
            options.commandString = Options.commandStringDefault;
        }
        if (!options.commandString.startsWith("/")) {
            options.commandString = "/" + options.commandString;
        }
        if (containsDuplicate(options.actions)) {
            options.actions = Options.actionsDefault.get();
        }
    }

    private boolean containsDuplicate(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[j].equals(array[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    // Load and save

    public static @NotNull Config load() {
        Path file = CONFIG_DIR.resolve(FILE_NAME);
        Config config = null;
        if (Files.exists(file)) {
            config = load(file, GSON);
            if (config == null) {
                backup();
                Flashside.LOG.warn("Resetting config");
            }
        }
        return config != null ? config : new Config();
    }

    @SuppressWarnings("SameParameterValue")
    private static @Nullable Config load(Path file, Gson gson) {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Config.class);
        } catch (Exception e) {
            // Catch Exception as errors in deserialization may not fall under
            // IOException or JsonParseException, but should not crash the game.
            Flashside.LOG.error("Unable to load config", e);
            return null;
        }
    }

    private static void backup() {
        try {
            Flashside.LOG.warn("Copying {} to {}", FILE_NAME, BACKUP_FILE_NAME);
            if (!Files.isDirectory(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            Path file = CONFIG_DIR.resolve(FILE_NAME);
            Path backupFile = file.resolveSibling(BACKUP_FILE_NAME);
            Files.move(file, backupFile, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Flashside.LOG.error("Unable to copy config file", e);
        }
    }

    public static void save() {
        if (instance == null) return;
        instance.validate();
        try {
            if (!Files.isDirectory(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            Path file = CONFIG_DIR.resolve(FILE_NAME);
            Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(tempFile.toFile()), StandardCharsets.UTF_8)) {
                writer.write(GSON.toJson(instance));
            } catch (IOException e) {
                throw new IOException(e);
            }
            Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            Flashside.onConfigSaved(instance);
        } catch (IOException e) {
            Flashside.LOG.error("Unable to save config", e);
        }
    }
}
