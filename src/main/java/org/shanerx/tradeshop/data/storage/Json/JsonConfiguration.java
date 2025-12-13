/*
 *
 *                         Copyright (c) 2016-2023
 *                SparklingComet @ http://shanerx.org
 *               KillerOfPie @ http://killerofpie.github.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *                http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NOTICE: All modifications made by others to the source code belong
 *  to the respective contributor. No contributor should be held liable for
 *  any damages of any kind, whether be material or moral, which were
 *  caused by their contribution(s) to the project. See the full License for more information.
 *
 */

package org.shanerx.tradeshop.data.storage.Json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

class JsonConfiguration extends Utils {
    protected final GsonProcessor gson;
    protected File file, pathFile;
    protected JsonObject jsonObj;

    private final TradeShop PLUGIN = TradeShop.getPlugin();
    private static final String TEMP_FOLDER_NAME = ".tmp";

    /**
     * Creates a JsonConfiguration object assisting with managing JSON data
     *
     * @param folderFromData Directory path of the file
     * @param fileName       name of the file to load without extension
     */
    protected JsonConfiguration(String folderFromData, String fileName) {
        this.gson = new GsonProcessor();
        this.pathFile = getPath(folderFromData);
        this.file = getFile(folderFromData, fileName);

        buildFilePath();
        loadFile();
    }

    public static File getFile(String folderFromData, String fileName) {
        return new File(getPath(folderFromData).getPath() + File.separator + fileName + ".json");
    }

    public static File getPath(String folderFromData) {
        return new File(TradeShop.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + folderFromData);
    }

    /**
     * Gets the temporary folder for storing temporary files during writes.
     * Creates the folder if it doesn't exist.
     */
    private File getTempFolder() {
        File tempFolder = new File(TradeShop.getPlugin().getDataFolder().getAbsolutePath() + File.separator + TEMP_FOLDER_NAME);
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }
        return tempFolder;
    }

    private void buildFilePath() {
        try {
            pathFile.mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not create " + file.getName() + " file! Data may be lost!", e);
        }
    }

    protected void loadFile() {
        try (FileReader fileReader = new FileReader(file)) {
            jsonObj = new JsonParser().parse(fileReader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not load " + file.getName() + " file! Data may be lost!", e);
            jsonObj = new JsonObject();
        } catch (IllegalStateException e) {
            jsonObj = new JsonObject();
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not read " + file.getName() + " file!", e);
            jsonObj = new JsonObject();
        }
    }

    /**
     * Saves the JSON object to file using atomic write operation.
     * This prevents data corruption by writing to a temporary file first,
     * then atomically replacing the original file.
     *
     * This approach resolves issues with enchanted books and other complex
     * serializable items that may cause partial writes or file corruption.
     */
    protected void saveFile() {
        String str = gson.toJson(jsonObj);
        if (str.isEmpty()) {
            return;
        }

        File tempFile = null;
        try {
            // Create temporary file in a separate .tmp folder
            // This prevents the temporary files from interfering with deserialization logic
            File tempFolder = getTempFolder();
            tempFile = File.createTempFile(file.getName() + "_", ".tmp", tempFolder);

            // Write data to temporary file
            try (FileWriter fileWriter = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
                fileWriter.write(str);
                fileWriter.flush();
            }

            // Verify the temporary file was written correctly before replacing
            if (tempFile.length() == 0) {
                PLUGIN.getLogger().log(Level.SEVERE, "Failed to write to temporary file for " + file.getName() + "! File may be corrupted.");
                if (tempFile.delete()) {
                    PLUGIN.getLogger().log(Level.INFO, "Cleaned up empty temporary file: " + tempFile.getPath());
                }
                return;
            }

            // Attempt to replace the file with retry logic for Windows file locking
            replaceFileWithRetry(tempFile, file);

        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not save " + file.getName() + " file! Data may be lost!", e);

            // Cleanup temporary file on error
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    PLUGIN.getLogger().log(Level.WARNING, "Failed to cleanup temporary file: " + tempFile.getPath());
                }
            }
        }
    }

    /**
     * Attempts to replace the target file with the temporary file, with retry logic.
     * This handles Windows file locking issues where files may be temporarily in use.
     */
    private void replaceFileWithRetry(File tempFile, File targetFile) {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 100;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Try atomic move first
                try {
                    Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    return; // Success
                } catch (IOException atomicMoveException) {
                    // Atomic move failed, try regular move (non-atomic)
                    try {
                        Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        return; // Success
                    } catch (IOException regularMoveException) {
                        // Both methods failed, throw to retry logic
                        throw regularMoveException;
                    }
                }

            } catch (IOException e) {
                // If this is the last attempt, log error and keep temp file
                if (attempt == MAX_RETRIES) {
                    PLUGIN.getLogger().log(Level.SEVERE, "Could not save " + targetFile.getName() + " file after " + MAX_RETRIES + " attempts! Temp file available at: " + tempFile.getAbsolutePath(), e);
                    return;
                }

                // Wait before retrying (gives file handles time to release)
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    PLUGIN.getLogger().log(Level.WARNING, "File save retry interrupted for " + targetFile.getName());
                    return;
                }
            }
        }
    }
}
