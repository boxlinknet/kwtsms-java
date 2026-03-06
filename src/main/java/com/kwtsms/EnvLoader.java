package com.kwtsms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal .env file parser. Never throws, never modifies system environment.
 */
public final class EnvLoader {

    private EnvLoader() {}

    /**
     * Parse a .env file and return a map of key-value pairs.
     *
     * Rules:
     * - Ignores blank lines and lines starting with #
     * - Strips inline # comments from unquoted values
     * - Supports single-quoted and double-quoted values (preserves # inside quotes)
     * - Returns empty map for missing files
     * - Never modifies the system environment
     *
     * @param filePath path to the .env file (defaults to ".env")
     * @return unmodifiable map of parsed key-value pairs
     */
    public static Map<String, String> loadEnvFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String rawLine;
            while ((rawLine = reader.readLine()) != null) {
                String line = rawLine.trim();

                // Skip blank lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Find the first = sign
                int eqIndex = line.indexOf('=');
                if (eqIndex < 0) {
                    continue;
                }

                String key = line.substring(0, eqIndex).trim();
                if (key.isEmpty()) {
                    continue;
                }

                String value = line.substring(eqIndex + 1).trim();

                // Handle quoted values
                if (value.length() >= 2) {
                    char first = value.charAt(0);
                    char last = value.charAt(value.length() - 1);
                    if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                        result.put(key, value.substring(1, value.length() - 1));
                        continue;
                    }
                }

                // Strip inline comments for unquoted values
                int commentIndex = value.indexOf('#');
                if (commentIndex > 0) {
                    value = value.substring(0, commentIndex).trim();
                }

                result.put(key, value);
            }
        } catch (Exception e) {
            // Never crash on file read errors
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Parse .env file at the default path ".env".
     */
    public static Map<String, String> loadEnvFile() {
        return loadEnvFile(".env");
    }
}
