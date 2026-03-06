package com.kwtsms;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Integration tests that hit the live kwtSMS API with test_mode=true.
 * Skipped if JAVA_USERNAME / JAVA_PASSWORD are not set.
 * No credits are consumed (test mode).
 */
@EnabledIfEnvironmentVariable(named = "JAVA_USERNAME", matches = ".+")
class IntegrationTest {

    private static KwtSMS sms;
    private static KwtSMS badSms;

    @BeforeAll
    static void setUp() {
        String username = System.getenv("JAVA_USERNAME");
        String password = System.getenv("JAVA_PASSWORD");
        sms = new KwtSMS(username, password, "KWT-SMS", true, "");
        badSms = new KwtSMS("wrong_user", "wrong_pass", "KWT-SMS", true, "");
    }

    // ── verify ──

    @Test
    void verify_validCredentials() {
        VerifyResult result = sms.verify();
        assertTrue(result.isOk(), "verify() should succeed with valid credentials");
        assertNotNull(result.getBalance(), "Balance should be a number");
        assertTrue(result.getBalance() >= 0, "Balance should be non-negative");
    }

    @Test
    void verify_wrongCredentials() {
        VerifyResult result = badSms.verify();
        assertFalse(result.isOk(), "verify() should fail with wrong credentials");
        assertNotNull(result.getError(), "Should have an error message");
    }

    // ── balance ──

    @Test
    void balance_returnsNumber() {
        Double bal = sms.balance();
        assertNotNull(bal, "Balance should not be null");
        assertTrue(bal >= 0, "Balance should be non-negative");
    }

    // ── send ──

    @Test
    void send_validKuwaitNumber() {
        SendResult result = sms.send("96598765432", "Java integration test");
        assertNotNull(result.getResult());
        // In test mode, result can be OK or ERROR depending on account config
    }

    @Test
    void send_invalidInputEmail() {
        SendResult result = sms.send("user@example.com", "Test");
        assertEquals("ERROR", result.getResult());
        assertFalse(result.getInvalid().isEmpty(), "Should have invalid entries");
    }

    @Test
    void send_invalidInputTooShort() {
        SendResult result = sms.send("123", "Test");
        assertEquals("ERROR", result.getResult());
        assertFalse(result.getInvalid().isEmpty());
    }

    @Test
    void send_invalidInputLetters() {
        SendResult result = sms.send("abcdefgh", "Test");
        assertEquals("ERROR", result.getResult());
    }

    @Test
    void send_mixedValidAndInvalid() {
        SendResult result = sms.send("96598765432,invalid,user@email.com", "Test mixed");
        // Valid number should be sent, invalid ones reported
        assertNotNull(result.getResult());
        assertFalse(result.getInvalid().isEmpty(), "Should report invalid entries");
    }

    @Test
    void send_normalizationPlusPrefix() {
        SendResult result = sms.send("+96598765432", "Test plus prefix");
        assertNotNull(result.getResult());
    }

    @Test
    void send_normalization00Prefix() {
        SendResult result = sms.send("0096598765432", "Test 00 prefix");
        assertNotNull(result.getResult());
    }

    @Test
    void send_normalizationArabicDigits() {
        SendResult result = sms.send("\u0669\u0666\u0665\u0669\u0668\u0667\u0666\u0665\u0664\u0663\u0662", "Test Arabic digits");
        assertNotNull(result.getResult());
    }

    @Test
    void send_duplicateNormalized() {
        // +96598765432 and 0096598765432 normalize to the same number
        SendResult result = sms.send("+96598765432,0096598765432", "Test dedup");
        assertNotNull(result.getResult());
        // Should be deduplicated: only 1 number sent
    }

    @Test
    void send_emptyMessage() {
        SendResult result = sms.send("96598765432", "");
        assertEquals("ERROR", result.getResult());
    }

    @Test
    void send_emojiOnlyMessage() {
        SendResult result = sms.send("96598765432", "\uD83D\uDE00\uD83D\uDE01\uD83D\uDE02");
        assertEquals("ERROR", result.getResult());
        // Message empty after cleaning
    }

    // ── senderIds ──

    @Test
    void senderIds_returnsList() {
        SenderIdResult result = sms.senderIds();
        assertEquals("OK", result.getResult());
        assertNotNull(result.getSenderIds());
        assertFalse(result.getSenderIds().isEmpty(), "Should have at least one sender ID");
    }

    // ── coverage ──

    @Test
    void coverage_returnsPrefixes() {
        CoverageResult result = sms.coverage();
        assertEquals("OK", result.getResult());
        assertNotNull(result.getPrefixes());
    }

    // ── validate ──

    @Test
    void validate_validNumbers() {
        ValidateResult result = sms.validate(Arrays.asList("96598765432"));
        assertNotNull(result);
        // API should return OK/ER/NR breakdown
    }

    @Test
    void validate_mixedInputs() {
        ValidateResult result = sms.validate(
                Arrays.asList("96598765432", "invalid", "user@email.com"));
        assertNotNull(result);
        assertFalse(result.getRejected().isEmpty(), "Should have rejected entries");
    }

    // ── status (may return error if no valid msgId) ──

    @Test
    void status_invalidMsgId() {
        StatusResult result = sms.status("nonexistent_msgid");
        assertEquals("ERROR", result.getResult());
    }

    // ── deliveryReport ──

    @Test
    void deliveryReport_invalidMsgId() {
        DeliveryReportResult result = sms.deliveryReport("nonexistent_msgid");
        assertEquals("ERROR", result.getResult());
    }

    // ── wrong sender ID ──

    @Test
    void send_wrongSenderId() {
        SendResult result = sms.send("96598765432", "Test wrong sender", "NONEXISTENT-SENDER");
        // Should return error about sender ID
        assertNotNull(result.getResult());
    }

    // ── empty sender ID ──

    @Test
    void send_emptySenderId() {
        SendResult result = sms.send("96598765432", "Test empty sender", "");
        assertNotNull(result.getResult());
    }
}
