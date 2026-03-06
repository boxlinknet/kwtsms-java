package com.kwtsms;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Integration tests for the CLI tool.
 * Runs CLI commands via subprocess so System.exit() does not kill the test runner.
 * Skipped if JAVA_USERNAME / JAVA_PASSWORD are not set.
 *
 * balance() is called ONCE in BeforeAll. Tests run in order and track
 * expectedBalance so every CLI send can be verified without extra API calls.
 */
@EnabledIfEnvironmentVariable(named = "JAVA_USERNAME", matches = ".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CLIIntegrationTest {

    private static KwtSMS sms;
    private static String javaCmd;
    private static String classpath;
    private static String username;
    private static String password;

    /** Balance retrieved once in BeforeAll. Updated after CLI sends. */
    private static double expectedBalance;

    // cli_send_testMode consumes 1 credit
    private static final int TOTAL_CREDITS_NEEDED = 1;

    @BeforeAll
    static void setUp() {
        username = System.getenv("JAVA_USERNAME");
        password = System.getenv("JAVA_PASSWORD");
        javaCmd = System.getProperty("java.home") + "/bin/java";
        classpath = System.getProperty("java.class.path");
        sms = new KwtSMS(username, password, "KWT-SMS", true, "");

        // ONE balance call for the entire test class
        Double bal = sms.balance();
        assertNotNull(bal, "Should retrieve starting balance");
        expectedBalance = bal;
        System.out.println("=== CLIIntegrationTest: starting balance = " + expectedBalance
                + " | credits needed = " + TOTAL_CREDITS_NEEDED + " ===");
    }

    /**
     * Run CLI with given args, passing credentials via KWTSMS_ env vars.
     * Returns [exitCode, stdout, stderr].
     */
    private Object[] runCLI(String... args) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add(javaCmd);
        cmd.add("-cp");
        cmd.add(classpath);
        cmd.add("com.kwtsms.CLI");
        for (String arg : args) {
            cmd.add(arg);
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("KWTSMS_USERNAME", username);
        env.put("KWTSMS_PASSWORD", password);
        env.put("KWTSMS_SENDER_ID", "KWT-SMS");
        env.put("KWTSMS_TEST_MODE", "1");
        env.put("KWTSMS_LOG_FILE", "");

        Process proc = pb.start();

        StringBuilder stdout = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stdout.append(line).append("\n");
            }
        }

        StringBuilder stderr = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stderr.append(line).append("\n");
            }
        }

        int exitCode = proc.waitFor();
        return new Object[]{exitCode, stdout.toString(), stderr.toString()};
    }

    // ── 1. No API / read-only (0 credits) ──

    @Test @Order(1)
    void cli_help() throws Exception {
        Object[] result = runCLI("--help");
        assertEquals(0, (int) result[0], "help should exit 0");
        String stdout = (String) result[1];
        assertTrue(stdout.contains("setup"), "Should list setup command");
        assertTrue(stdout.contains("verify"), "Should list verify command");
        assertTrue(stdout.contains("send"), "Should list send command");
        assertTrue(stdout.contains("balance"), "Should list balance command");
    }

    @Test @Order(2)
    void cli_unknownCommand() throws Exception {
        Object[] result = runCLI("nonexistent");
        assertNotEquals(0, (int) result[0], "unknown command should exit non-zero");
        assertTrue(((String) result[1]).contains("Unknown command"), "Should say unknown command");
    }

    @Test @Order(3)
    void cli_send_missingArgs() throws Exception {
        Object[] result = runCLI("send");
        assertNotEquals(0, (int) result[0], "send without args should exit non-zero");
    }

    @Test @Order(4)
    void cli_verify() throws Exception {
        Object[] result = runCLI("verify");
        String stdout = (String) result[1];
        System.out.println("CLI verify: " + stdout.trim());
        assertEquals(0, (int) result[0], "verify should exit 0");
        assertTrue(stdout.contains("Credentials valid"), "Should show credentials valid");
        assertTrue(stdout.contains("Available:"), "Should show available balance");
    }

    @Test @Order(5)
    void cli_balance() throws Exception {
        Object[] result = runCLI("balance");
        String stdout = (String) result[1];
        System.out.println("CLI balance: " + stdout.trim());
        assertEquals(0, (int) result[0], "balance should exit 0");
        assertTrue(stdout.contains("Available:"), "Should show available balance");
    }

    @Test @Order(6)
    void cli_senderid() throws Exception {
        Object[] result = runCLI("senderid");
        String stdout = (String) result[1];
        System.out.println("CLI senderid: " + stdout.trim());
        assertEquals(0, (int) result[0], "senderid should exit 0");
        assertTrue(stdout.contains("Sender IDs on this account:") || stdout.contains("No sender IDs"),
                "Should list sender IDs or say none");
    }

    @Test @Order(7)
    void cli_coverage() throws Exception {
        Object[] result = runCLI("coverage");
        String stdout = (String) result[1];
        System.out.println("CLI coverage: " + stdout.trim());
        assertEquals(0, (int) result[0], "coverage should exit 0");
        assertTrue(stdout.contains("Active country prefixes"), "Should list country prefixes");
    }

    @Test @Order(8)
    void cli_validate() throws Exception {
        Object[] result = runCLI("validate", "96599220001", "+96512345678");
        String stdout = (String) result[1];
        System.out.println("CLI validate: " + stdout.trim());
        assertEquals(0, (int) result[0], "validate should exit 0");
        assertTrue(stdout.contains("OK") || stdout.contains("ER") || stdout.contains("NR"),
                "Should show validation breakdown");
    }

    // ── 2. Verify balance unchanged after read-only CLI commands ──

    @Test @Order(9)
    void balance_snapshotBeforeSend() {
        Double bal = sms.balance();
        assertNotNull(bal, "Should retrieve balance");
        expectedBalance = bal;
        System.out.println("Balance snapshot before CLI send: " + bal);
    }

    // ── 3. Send (1 credit) ──

    @Test @Order(10)
    void cli_send_testMode() throws Exception {
        assumeTrue(expectedBalance >= TOTAL_CREDITS_NEEDED,
                "Insufficient balance: " + expectedBalance + " < " + TOTAL_CREDITS_NEEDED);
        System.out.println("cli_send_testMode | balance before: " + expectedBalance);

        Object[] result = runCLI("send", "96599220600", "CLI integration test");
        String stdout = (String) result[1];

        System.out.println("CLI send: " + stdout.trim());
        assertEquals(0, (int) result[0], "send should exit 0 in test mode");
        assertTrue(stdout.contains("TEST MODE"), "Should show test mode warning");
        assertTrue(stdout.contains("Sent") && stdout.contains("msg-id"), "Should show sent confirmation");

        expectedBalance -= 1;
        System.out.println("cli_send_testMode | -1 | expected balance: " + expectedBalance);
    }

    // ── 4. Final balance verification ──

    @Test @Order(999)
    void balance_finalVerification() {
        Double bal = sms.balance();
        assertEquals(expectedBalance, bal, 0.01,
                "Final balance should match tracked expected balance");
        System.out.println("=== CLI final balance: " + bal + " (expected: " + expectedBalance + ") ===");
    }
}
