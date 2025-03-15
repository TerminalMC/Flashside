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
        
        // Title screen options

        public static final boolean editTitleScreenDefault = false;
        public boolean editTitleScreen = editTitleScreenDefault;

        public static final int startRowTitleScreenDefault = 1;
        public int startRowTitleScreen = startRowTitleScreenDefault;
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
        // Called before config is saved
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
