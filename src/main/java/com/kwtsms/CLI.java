package com.kwtsms;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * CLI entry point for the kwtsms command.
 *
 * <pre>
 * Usage:
 *     kwtsms setup
 *     kwtsms verify
 *     kwtsms balance
 *     kwtsms senderid
 *     kwtsms coverage
 *     kwtsms send 96598765432 "Your OTP for MYAPP is: 123456"
 *     kwtsms send 96598765432,96512345678 "Hello!" --sender "MY APP"
 *     kwtsms validate 96598765432 +96512345678 0096511111111
 * </pre>
 */
public final class CLI {

    private static final String TEST_MODE_WARNING =
            "\n  TEST MODE: message will be queued but NOT delivered to the handset.\n" +
            "  No SMS credits will be consumed.\n" +
            "  To send real messages run 'kwtsms setup' and choose Live mode.\n";

    private static final String USAGE =
            "Usage: kwtsms <command> [options]\n\n" +
            "Commands:\n" +
            "  setup                          Interactive first-time setup wizard\n" +
            "  verify                         Test credentials and show balance\n" +
            "  balance                        Show current SMS credit balance\n" +
            "  senderid                       List available sender IDs\n" +
            "  coverage                       List active country prefixes\n" +
            "  send <mobile> <message>        Send SMS (--sender to override sender ID)\n" +
            "  validate <number> [number...]  Validate phone numbers via API\n";

    private CLI() {}

    public static void main(String[] args) {
        if (args.length == 0 || "-h".equals(args[0]) || "--help".equals(args[0])) {
            System.out.println(USAGE);
            System.exit(0);
        }

        String cmd = args[0].toLowerCase();

        if ("setup".equals(cmd)) {
            runSetup(".env");
            System.exit(0);
        }

        // All other commands need credentials
        KwtSMS sms;
        try {
            sms = KwtSMS.fromEnv();
        } catch (Exception e) {
            if (!new File(".env").exists()) {
                System.out.println("No .env file found. Starting first-time setup...\n");
                runSetup(".env");
                try {
                    sms = KwtSMS.fromEnv();
                } catch (Exception e2) {
                    System.out.println("Error: " + e2.getMessage());
                    System.exit(1);
                    return;
                }
            } else {
                System.out.println("Error: credentials missing or incomplete in .env");
                System.out.println("Run 'kwtsms setup' to fix.");
                System.exit(1);
                return;
            }
        }

        if ("verify".equals(cmd)) {
            cmdVerify(sms);
        } else if ("balance".equals(cmd)) {
            cmdBalance(sms);
        } else if ("send".equals(cmd)) {
            cmdSend(sms, Arrays.copyOfRange(args, 1, args.length));
        } else if ("validate".equals(cmd)) {
            cmdValidate(sms, Arrays.copyOfRange(args, 1, args.length));
        } else if ("senderid".equals(cmd)) {
            cmdSenderId(sms);
        } else if ("coverage".equals(cmd)) {
            cmdCoverage(sms);
        } else {
            System.out.println("Unknown command: " + cmd);
            System.out.println(USAGE);
            System.exit(1);
        }
    }

    // ── Setup Wizard ──

    private static void runSetup(String envFile) {
        Scanner scanner = new Scanner(System.in, "UTF-8");

        System.out.println();
        System.out.println("-- kwtSMS Setup ----------------------------------------------------------");
        System.out.println("Verifies your API credentials and creates a .env file.");
        System.out.println("Press Enter to keep the value shown in brackets.");
        System.out.println();

        Map<String, String> existing = EnvLoader.loadEnvFile(envFile);

        // Username
        String defaultUser = existing.containsKey("KWTSMS_USERNAME") ? existing.get("KWTSMS_USERNAME") : "";
        String username;
        if (!defaultUser.isEmpty()) {
            System.out.print("API Username [" + defaultUser + "]: ");
        } else {
            System.out.print("API Username: ");
        }
        System.out.flush();
        username = scanner.nextLine().trim();
        if (username.isEmpty()) username = defaultUser;

        // Password (hidden if console available, visible fallback)
        String defaultPass = existing.containsKey("KWTSMS_PASSWORD") ? existing.get("KWTSMS_PASSWORD") : "";
        String password;
        Console console = System.console();
        if (console != null) {
            char[] passChars;
            if (!defaultPass.isEmpty()) {
                passChars = console.readPassword("API Password [keep existing]: ");
            } else {
                passChars = console.readPassword("API Password: ");
            }
            password = (passChars != null && passChars.length > 0) ? new String(passChars).trim() : defaultPass;
        } else {
            // Fallback for IDE / redirected stdin
            if (!defaultPass.isEmpty()) {
                System.out.print("API Password [keep existing]: ");
            } else {
                System.out.print("API Password: ");
            }
            System.out.flush();
            password = scanner.nextLine().trim();
            if (password.isEmpty()) password = defaultPass;
        }

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("\nError: username and password are required.");
            System.exit(1);
        }

        // Verify credentials
        System.out.print("\nVerifying credentials... ");
        System.out.flush();
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("username", username);
            payload.put("password", password);
            Map<String, Object> data = ApiRequest.post("balance", payload, "");

            if ("OK".equals(str(data.get("result")))) {
                System.out.println("OK  (Balance: " + data.get("available") + ")");
            } else {
                String err = str(data.get("description"));
                if (err == null) err = str(data.get("code"));
                if (err == null) err = "Unknown error";
                System.out.println("FAILED\nError: " + err);
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("FAILED\nError: " + e.getMessage());
            System.exit(1);
        }

        // Fetch Sender IDs
        System.out.print("Fetching Sender IDs... ");
        System.out.flush();
        List<String> senderIds = new ArrayList<>();
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("username", username);
            payload.put("password", password);
            Map<String, Object> sidData = ApiRequest.post("senderid", payload, "");
            if ("OK".equals(str(sidData.get("result")))) {
                Object sidObj = sidData.get("senderid");
                if (sidObj instanceof List) {
                    for (Object item : (List<?>) sidObj) {
                        if (item != null) senderIds.add(item.toString());
                    }
                }
            }
        } catch (Exception ignored) {
        }

        String senderId;
        if (!senderIds.isEmpty()) {
            System.out.println("OK");
            System.out.println("\nAvailable Sender IDs:");
            for (int i = 0; i < senderIds.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + senderIds.get(i));
            }
            String defaultSid = existing.containsKey("KWTSMS_SENDER_ID")
                    ? existing.get("KWTSMS_SENDER_ID") : senderIds.get(0);
            System.out.print("\nSelect Sender ID (number or name) [" + defaultSid + "]: ");
            System.out.flush();
            String choice = scanner.nextLine().trim();
            if (!choice.isEmpty() && isDigits(choice)) {
                int idx = Integer.parseInt(choice);
                if (idx >= 1 && idx <= senderIds.size()) {
                    senderId = senderIds.get(idx - 1);
                } else {
                    senderId = defaultSid;
                }
            } else if (!choice.isEmpty()) {
                senderId = choice;
            } else {
                senderId = defaultSid;
            }
        } else {
            System.out.println("(none returned)");
            String defaultSid = existing.containsKey("KWTSMS_SENDER_ID")
                    ? existing.get("KWTSMS_SENDER_ID") : "KWT-SMS";
            System.out.print("Sender ID [" + defaultSid + "]: ");
            System.out.flush();
            senderId = scanner.nextLine().trim();
            if (senderId.isEmpty()) senderId = defaultSid;
        }

        // Send mode
        String currentMode = existing.containsKey("KWTSMS_TEST_MODE") ? existing.get("KWTSMS_TEST_MODE") : "1";
        System.out.println("\nSend mode:");
        System.out.println("  1. Test mode: messages queued but NOT delivered, no credits consumed  [default]");
        System.out.println("  2. Live mode: messages delivered to handsets, credits consumed");
        String modeDefault = "0".equals(currentMode) ? "2" : "1";
        System.out.print("\nChoose [" + modeDefault + "]: ");
        System.out.flush();
        String modeChoice = scanner.nextLine().trim();
        if (modeChoice.isEmpty()) modeChoice = modeDefault;
        String testMode = "2".equals(modeChoice) ? "0" : "1";

        if ("1".equals(testMode)) {
            System.out.println("  -> Test mode selected.");
        } else {
            System.out.println("  -> Live mode selected. Real messages will be sent and credits consumed.");
        }

        // Log file
        String defaultLog = existing.containsKey("KWTSMS_LOG_FILE") ? existing.get("KWTSMS_LOG_FILE") : "kwtsms.log";
        System.out.println("\nAPI logging (every API call is logged to a file, passwords are always masked):");
        if (!defaultLog.isEmpty()) {
            System.out.println("  Current: " + defaultLog);
        }
        System.out.println("  Type \"off\" to disable logging.");
        System.out.print("  Log file path [" + (defaultLog.isEmpty() ? "off" : defaultLog) + "]: ");
        System.out.flush();
        String logInput = scanner.nextLine().trim();
        String logFilePath;
        if ("off".equalsIgnoreCase(logInput)) {
            logFilePath = "";
            System.out.println("  -> Logging disabled.");
        } else if (!logInput.isEmpty()) {
            logFilePath = logInput;
        } else {
            logFilePath = defaultLog;
        }

        // Sanitize: strip newlines to prevent values from breaking .env file format
        username = sanitizeLine(username);
        password = sanitizeLine(password);
        senderId = sanitizeLine(senderId);
        logFilePath = sanitizeLine(logFilePath);

        // Write .env
        String content =
                "# kwtSMS credentials, generated by kwtsms setup\n" +
                "KWTSMS_USERNAME=" + username + "\n" +
                "KWTSMS_PASSWORD=" + password + "\n" +
                "KWTSMS_SENDER_ID=" + senderId + "\n" +
                "KWTSMS_TEST_MODE=" + testMode + "\n" +
                "KWTSMS_LOG_FILE=" + logFilePath + "\n";

        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(envFile), StandardCharsets.UTF_8));
            writer.write(content);
            writer.close();

            // Try to set file permissions to owner-only (Java 8 compatible)
            File f = new File(envFile);
            f.setReadable(false, false);
            f.setReadable(true, true);
            f.setWritable(false, false);
            f.setWritable(true, true);
        } catch (Exception e) {
            System.out.println("\nError writing " + envFile + ": " + e.getMessage());
            System.exit(1);
        }

        System.out.println("\n  Saved to " + envFile);
        if ("1".equals(testMode)) {
            System.out.println("  Mode: TEST: messages queued but not delivered (no credits consumed)");
        } else {
            System.out.println("  Mode: LIVE: messages will be delivered and credits consumed");
        }
        System.out.println("  Run 'kwtsms setup' at any time to change settings.");
        System.out.println("-------------------------------------------------------------------------\n");
    }

    // ── Commands ──

    private static void cmdVerify(KwtSMS sms) {
        VerifyResult r = sms.verify();
        if (r.isOk()) {
            Double purchased = sms.getCachedPurchased();
            System.out.println("  Credentials valid  |  Available: " + r.getBalance() +
                    "  |  Purchased: " + (purchased != null ? purchased : "?"));
        } else {
            System.out.println("  Credential check failed: " + r.getError());
            System.exit(1);
        }
    }

    private static void cmdBalance(KwtSMS sms) {
        VerifyResult r = sms.verify();
        if (r.isOk()) {
            Double purchased = sms.getCachedPurchased();
            System.out.println("  Available: " + r.getBalance() +
                    "  |  Purchased: " + (purchased != null ? purchased : "?"));
        } else {
            System.out.println("  Could not retrieve balance: " + r.getError());
            System.exit(1);
        }
    }

    private static void cmdSend(KwtSMS sms, String[] args) {
        // Parse: <mobile> <message> [--sender SENDER_ID]
        if (args.length < 2) {
            System.out.println("  Usage: kwtsms send <mobile> <message> [--sender SENDER_ID]");
            System.out.println("         kwtsms send 96598765432,96512345678 \"Hello!\" --sender \"MY APP\"");
            System.exit(1);
            return;
        }

        String mobile = args[0];
        String message = args[1];
        String sender = null;

        for (int i = 2; i < args.length; i++) {
            if ("--sender".equals(args[i]) && i + 1 < args.length) {
                sender = args[i + 1];
                i++;
            }
        }

        // Detect test mode from env var or .env file
        String testModeEnv = System.getenv("KWTSMS_TEST_MODE");
        if (testModeEnv == null) {
            testModeEnv = EnvLoader.loadEnvFile(".env").get("KWTSMS_TEST_MODE");
        }
        boolean isTestMode = "1".equals(testModeEnv);
        if (isTestMode) {
            System.out.println(TEST_MODE_WARNING);
        }

        SendResult result = sms.send(mobile, message, sender);

        if (!result.getInvalid().isEmpty()) {
            for (InvalidEntry inv : result.getInvalid()) {
                System.out.println("  Skipped: " + inv.getInput() + ": " + inv.getError());
            }
        }

        if ("OK".equals(result.getResult())) {
            System.out.println("  Sent  |  msg-id: " + result.getMsgId() +
                    "  |  balance-after: " + result.getBalanceAfter());
            if (isTestMode) {
                System.out.println("  (test send: check kwtsms.com Queue to confirm; delete to recover credits)");
            }
        } else {
            System.out.println("  Failed: " + result.getCode() + ": " + result.getDescription());
            if (result.getAction() != null) {
                System.out.println("  Action: " + result.getAction());
            }
            System.exit(1);
        }
    }

    private static void cmdValidate(KwtSMS sms, String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: kwtsms validate <number> [number2 ...]");
            System.exit(1);
            return;
        }

        List<String> numbers = Arrays.asList(args);
        ValidateResult report = sms.validate(numbers);

        System.out.println("Valid    (OK): " + report.getOk());
        System.out.println("Invalid  (ER): " + report.getEr());
        System.out.println("No route (NR): " + report.getNr());

        if (!report.getRejected().isEmpty()) {
            for (InvalidEntry r : report.getRejected()) {
                System.out.println("  Rejected: " + r.getInput() + ": " + r.getError());
            }
        }
        if (report.getError() != null) {
            System.out.println("  Error: " + report.getError());
        }
    }

    private static void cmdSenderId(KwtSMS sms) {
        SenderIdResult result = sms.senderIds();
        if ("OK".equals(result.getResult())) {
            List<String> ids = result.getSenderIds();
            if (!ids.isEmpty()) {
                System.out.println("Sender IDs on this account:");
                for (String sid : ids) {
                    System.out.println("  " + sid);
                }
            } else {
                System.out.println("No sender IDs registered on this account.");
            }
        } else {
            System.out.println("  Error: " + (result.getDescription() != null ? result.getDescription() : result.getCode()));
            if (result.getAction() != null) {
                System.out.println("  Action: " + result.getAction());
            }
            System.exit(1);
        }
    }

    private static void cmdCoverage(KwtSMS sms) {
        CoverageResult result = sms.coverage();
        if ("OK".equals(result.getResult())) {
            List<String> prefixes = result.getPrefixes();
            System.out.println("Active country prefixes (" + prefixes.size() + "):");
            for (String p : prefixes) {
                System.out.println("  +" + p);
            }
        } else {
            System.out.println("  Error: " + (result.getDescription() != null ? result.getDescription() : result.getCode()));
            if (result.getAction() != null) {
                System.out.println("  Action: " + result.getAction());
            }
            System.exit(1);
        }
    }

    // ── Helpers ──

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean isDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return !s.isEmpty();
    }

    private static String sanitizeLine(String s) {
        return s.replace("\r", "").replace("\n", "");
    }
}
