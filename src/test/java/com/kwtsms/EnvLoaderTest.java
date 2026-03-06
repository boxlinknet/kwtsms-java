package com.kwtsms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Map;

class EnvLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadEnvFile_parsesBasicKeyValue() throws Exception {
        File envFile = createEnvFile("KEY=value\nOTHER=123\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals("value", result.get("KEY"));
        assertEquals("123", result.get("OTHER"));
    }

    @Test
    void loadEnvFile_skipsComments() throws Exception {
        File envFile = createEnvFile("# This is a comment\nKEY=value\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals(1, result.size());
        assertEquals("value", result.get("KEY"));
    }

    @Test
    void loadEnvFile_skipsBlankLines() throws Exception {
        File envFile = createEnvFile("KEY=value\n\n\nOTHER=123\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals(2, result.size());
    }

    @Test
    void loadEnvFile_stripsInlineComments() throws Exception {
        File envFile = createEnvFile("KEY=value # this is a comment\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals("value", result.get("KEY"));
    }

    @Test
    void loadEnvFile_preservesDoubleQuotedValues() throws Exception {
        File envFile = createEnvFile("KEY=\"value with spaces\"\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals("value with spaces", result.get("KEY"));
    }

    @Test
    void loadEnvFile_preservesSingleQuotedValues() throws Exception {
        File envFile = createEnvFile("KEY='value with spaces'\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals("value with spaces", result.get("KEY"));
    }

    @Test
    void loadEnvFile_quotedValuePreservesHash() throws Exception {
        File envFile = createEnvFile("KEY=\"value#with#hash\"\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals("value#with#hash", result.get("KEY"));
    }

    @Test
    void loadEnvFile_missingFile() {
        Map<String, String> result = EnvLoader.loadEnvFile("/nonexistent/.env");
        assertTrue(result.isEmpty());
    }

    @Test
    void loadEnvFile_emptyFile() throws Exception {
        File envFile = createEnvFile("");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertTrue(result.isEmpty());
    }

    @Test
    void loadEnvFile_noEqualsSign() throws Exception {
        File envFile = createEnvFile("INVALID_LINE\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertTrue(result.isEmpty());
    }

    @Test
    void loadEnvFile_emptyKey() throws Exception {
        File envFile = createEnvFile("=value\n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertTrue(result.isEmpty());
    }

    @Test
    void loadEnvFile_trimmedKeyAndValue() throws Exception {
        File envFile = createEnvFile("  KEY  =  value  \n");
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals("value", result.get("KEY"));
    }

    @Test
    void loadEnvFile_realWorldExample() throws Exception {
        File envFile = createEnvFile(
                "KWTSMS_USERNAME=myuser\n" +
                "KWTSMS_PASSWORD=mypass\n" +
                "KWTSMS_SENDER_ID=MY-APP  # set to your ID\n" +
                "KWTSMS_TEST_MODE=1\n" +
                "KWTSMS_LOG_FILE=kwtsms.log\n"
        );
        Map<String, String> result = EnvLoader.loadEnvFile(envFile.getAbsolutePath());
        assertEquals("myuser", result.get("KWTSMS_USERNAME"));
        assertEquals("mypass", result.get("KWTSMS_PASSWORD"));
        assertEquals("MY-APP", result.get("KWTSMS_SENDER_ID"));
        assertEquals("1", result.get("KWTSMS_TEST_MODE"));
    }

    private File createEnvFile(String content) throws Exception {
        File file = tempDir.resolve(".env").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }
}
