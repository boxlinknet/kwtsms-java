package com.kwtsms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class PhoneUtilsTest {

    // ── normalizePhone ──

    @Test
    void normalizePhone_stripsPlus() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("+96598765432"));
    }

    @Test
    void normalizePhone_strips00Prefix() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("0096598765432"));
    }

    @Test
    void normalizePhone_stripsSpaces() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("965 9876 5432"));
    }

    @Test
    void normalizePhone_stripsDashes() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("965-9876-5432"));
    }

    @Test
    void normalizePhone_stripsDots() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("965.9876.5432"));
    }

    @Test
    void normalizePhone_stripsParentheses() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("(965) 98765432"));
    }

    @Test
    void normalizePhone_convertsArabicIndicDigits() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("\u0669\u0666\u0665\u0669\u0668\u0667\u0666\u0665\u0664\u0663\u0662"));
    }

    @Test
    void normalizePhone_convertsExtendedArabicIndicDigits() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("\u06F9\u06F6\u06F5\u06F9\u06F8\u06F7\u06F6\u06F5\u06F4\u06F3\u06F2"));
    }

    @Test
    void normalizePhone_stripsLeadingZeros() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("0096598765432"));
    }

    @Test
    void normalizePhone_emptyString() {
        assertEquals("", PhoneUtils.normalizePhone(""));
    }

    @Test
    void normalizePhone_onlyZeros() {
        assertEquals("", PhoneUtils.normalizePhone("000"));
    }

    @Test
    void normalizePhone_nullInput() {
        assertEquals("", PhoneUtils.normalizePhone(null));
    }

    @Test
    void normalizePhone_lettersOnly() {
        assertEquals("", PhoneUtils.normalizePhone("abcdef"));
    }

    @Test
    void normalizePhone_mixedContent() {
        assertEquals("96598765432", PhoneUtils.normalizePhone("  +965 (98) 765-432  "));
    }

    // ── validatePhoneInput ──

    @Test
    void validatePhoneInput_validKuwaitNumber() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("96598765432");
        assertTrue(r.isValid());
        assertNull(r.getError());
        assertEquals("96598765432", r.getNormalized());
    }

    @Test
    void validatePhoneInput_validWithPlus() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("+96598765432");
        assertTrue(r.isValid());
        assertEquals("96598765432", r.getNormalized());
    }

    @Test
    void validatePhoneInput_validMinimum7Digits() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("1234567");
        assertTrue(r.isValid());
        assertEquals("1234567", r.getNormalized());
    }

    @Test
    void validatePhoneInput_validMaximum15Digits() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("123456789012345");
        assertTrue(r.isValid());
        assertEquals("123456789012345", r.getNormalized());
    }

    @Test
    void validatePhoneInput_empty() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("");
        assertFalse(r.isValid());
        assertEquals("Phone number is required", r.getError());
    }

    @Test
    void validatePhoneInput_blank() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("   ");
        assertFalse(r.isValid());
        assertEquals("Phone number is required", r.getError());
    }

    @Test
    void validatePhoneInput_email() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("user@example.com");
        assertFalse(r.isValid());
        assertTrue(r.getError().contains("email address"));
    }

    @Test
    void validatePhoneInput_noDigits() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("abcdef");
        assertFalse(r.isValid());
        assertTrue(r.getError().contains("no digits found"));
    }

    @Test
    void validatePhoneInput_tooShort() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("123");
        assertFalse(r.isValid());
        assertTrue(r.getError().contains("too short"));
        assertTrue(r.getError().contains("3 digits"));
    }

    @Test
    void validatePhoneInput_tooLong() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("1234567890123456");
        assertFalse(r.isValid());
        assertTrue(r.getError().contains("too long"));
        assertTrue(r.getError().contains("16 digits"));
    }

    @Test
    void validatePhoneInput_arabicDigits() {
        PhoneUtils.ValidationResult r = PhoneUtils.validatePhoneInput("\u0669\u0666\u0665\u0669\u0668\u0667\u0666\u0665\u0664\u0663\u0662");
        assertTrue(r.isValid());
        assertEquals("96598765432", r.getNormalized());
    }

    // ── deduplicatePhones ──

    @Test
    void deduplicatePhones_removesDuplicates() {
        List<String> result = PhoneUtils.deduplicatePhones(
                Arrays.asList("96598765432", "96512345678", "96598765432"));
        assertEquals(2, result.size());
        assertEquals("96598765432", result.get(0));
        assertEquals("96512345678", result.get(1));
    }

    @Test
    void deduplicatePhones_preservesOrder() {
        List<String> result = PhoneUtils.deduplicatePhones(
                Arrays.asList("96512345678", "96598765432", "96512345678"));
        assertEquals(2, result.size());
        assertEquals("96512345678", result.get(0));
        assertEquals("96598765432", result.get(1));
    }

    @Test
    void deduplicatePhones_emptyList() {
        List<String> result = PhoneUtils.deduplicatePhones(Arrays.asList());
        assertTrue(result.isEmpty());
    }
}
