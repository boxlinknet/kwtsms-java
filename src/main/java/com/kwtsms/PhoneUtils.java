package com.kwtsms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Phone number normalization and validation utilities.
 */
public final class PhoneUtils {

    private PhoneUtils() {}

    /**
     * Normalize a phone number to kwtSMS-accepted format (digits only, international format).
     *
     * 1. Converts Arabic-Indic (U+0660..U+0669) and Extended Arabic-Indic (U+06F0..U+06F9) digits to Latin
     * 2. Strips all non-digit characters
     * 3. Strips leading zeros
     *
     * @param phone raw phone number input
     * @return normalized phone number string
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(phone.length());
        for (int i = 0; i < phone.length(); i++) {
            char ch = phone.charAt(i);
            if (ch >= '\u0660' && ch <= '\u0669') {
                sb.append((char) ('0' + (ch - '\u0660')));
            } else if (ch >= '\u06F0' && ch <= '\u06F9') {
                sb.append((char) ('0' + (ch - '\u06F0')));
            } else if (ch >= '0' && ch <= '9') {
                sb.append(ch);
            }
        }
        // Strip leading zeros
        int start = 0;
        while (start < sb.length() && sb.charAt(start) == '0') {
            start++;
        }
        return start >= sb.length() ? "" : sb.substring(start);
    }

    /**
     * Validate a phone number input before sending to the API.
     *
     * @param phone raw phone number input
     * @return a ValidationResult with valid flag, error message, and normalized number
     */
    public static ValidationResult validatePhoneInput(String phone) {
        String raw = phone == null ? "" : phone.toString();
        String trimmed = raw.trim();

        if (trimmed.isEmpty()) {
            return new ValidationResult(false, "Phone number is required", "");
        }

        if (trimmed.contains("@")) {
            return new ValidationResult(false, "'" + trimmed + "' is an email address, not a phone number", "");
        }

        String normalized = normalizePhone(trimmed);

        if (normalized.isEmpty()) {
            return new ValidationResult(false, "'" + trimmed + "' is not a valid phone number, no digits found", "");
        }

        if (normalized.length() < 7) {
            return new ValidationResult(false, "'" + trimmed + "' is too short (" + normalized.length() + " digits, minimum is 7)", normalized);
        }

        if (normalized.length() > 15) {
            return new ValidationResult(false, "'" + trimmed + "' is too long (" + normalized.length() + " digits, maximum is 15)", normalized);
        }

        return new ValidationResult(true, null, normalized);
    }

    /**
     * Deduplicate a list of phone numbers while preserving order.
     *
     * @param phones list of normalized phone numbers
     * @return deduplicated list
     */
    public static List<String> deduplicatePhones(List<String> phones) {
        return new ArrayList<>(new LinkedHashSet<>(phones));
    }

    /**
     * Result of phone number validation.
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final String error;
        private final String normalized;

        public ValidationResult(boolean valid, String error, String normalized) {
            this.valid = valid;
            this.error = error;
            this.normalized = normalized;
        }

        public boolean isValid() { return valid; }
        public String getError() { return error; }
        public String getNormalized() { return normalized; }

        @Override
        public String toString() {
            return "ValidationResult{valid=" + valid + ", error='" + error + "', normalized='" + normalized + "'}";
        }
    }
}
