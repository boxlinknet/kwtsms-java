package com.kwtsms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Phone number normalization and validation utilities.
 */
public final class PhoneUtils {

    private PhoneUtils() {}

    // ── Phone validation rules by country code ──

    /**
     * Country-specific phone validation rules.
     * localLengths: valid digit count(s) AFTER country code.
     * mobileStartDigits: valid first character(s) of the local number (null = any).
     */
    static final Map<String, PhoneRule> PHONE_RULES;

    static final Map<String, String> COUNTRY_NAMES;

    static {
        Map<String, PhoneRule> rules = new HashMap<String, PhoneRule>();

        // GCC
        rules.put("965", new PhoneRule(new int[]{8}, new String[]{"4","5","6","9"}));       // Kuwait
        rules.put("966", new PhoneRule(new int[]{9}, new String[]{"5"}));                   // Saudi Arabia
        rules.put("971", new PhoneRule(new int[]{9}, new String[]{"5"}));                   // UAE
        rules.put("973", new PhoneRule(new int[]{8}, new String[]{"3","6"}));               // Bahrain
        rules.put("974", new PhoneRule(new int[]{8}, new String[]{"3","5","6","7"}));       // Qatar
        rules.put("968", new PhoneRule(new int[]{8}, new String[]{"7","9"}));               // Oman
        // Levant
        rules.put("962", new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Jordan
        rules.put("961", new PhoneRule(new int[]{7,8}, new String[]{"3","7","8"}));         // Lebanon
        rules.put("970", new PhoneRule(new int[]{9}, new String[]{"5"}));                   // Palestine
        rules.put("964", new PhoneRule(new int[]{10}, new String[]{"7"}));                  // Iraq
        rules.put("963", new PhoneRule(new int[]{9}, new String[]{"9"}));                   // Syria
        // Other Arab
        rules.put("967", new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Yemen
        rules.put("20",  new PhoneRule(new int[]{10}, new String[]{"1"}));                  // Egypt
        rules.put("218", new PhoneRule(new int[]{9}, new String[]{"9"}));                   // Libya
        rules.put("216", new PhoneRule(new int[]{8}, new String[]{"2","4","5","9"}));       // Tunisia
        rules.put("212", new PhoneRule(new int[]{9}, new String[]{"6","7"}));               // Morocco
        rules.put("213", new PhoneRule(new int[]{9}, new String[]{"5","6","7"}));           // Algeria
        rules.put("249", new PhoneRule(new int[]{9}, new String[]{"9"}));                   // Sudan
        // Non-Arab Middle East
        rules.put("98",  new PhoneRule(new int[]{10}, new String[]{"9"}));                  // Iran
        rules.put("90",  new PhoneRule(new int[]{10}, new String[]{"5"}));                  // Turkey
        rules.put("972", new PhoneRule(new int[]{9}, new String[]{"5"}));                   // Israel
        // South Asia
        rules.put("91",  new PhoneRule(new int[]{10}, new String[]{"6","7","8","9"}));      // India
        rules.put("92",  new PhoneRule(new int[]{10}, new String[]{"3"}));                  // Pakistan
        rules.put("880", new PhoneRule(new int[]{10}, new String[]{"1"}));                  // Bangladesh
        rules.put("94",  new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Sri Lanka
        rules.put("960", new PhoneRule(new int[]{7}, new String[]{"7","9"}));               // Maldives
        // East Asia
        rules.put("86",  new PhoneRule(new int[]{11}, new String[]{"1"}));                  // China
        rules.put("81",  new PhoneRule(new int[]{10}, new String[]{"7","8","9"}));          // Japan
        rules.put("82",  new PhoneRule(new int[]{10}, new String[]{"1"}));                  // South Korea
        rules.put("886", new PhoneRule(new int[]{9}, new String[]{"9"}));                   // Taiwan
        // Southeast Asia
        rules.put("65",  new PhoneRule(new int[]{8}, new String[]{"8","9"}));               // Singapore
        rules.put("60",  new PhoneRule(new int[]{9,10}, new String[]{"1"}));                // Malaysia
        rules.put("62",  new PhoneRule(new int[]{9,10,11,12}, new String[]{"8"}));          // Indonesia
        rules.put("63",  new PhoneRule(new int[]{10}, new String[]{"9"}));                  // Philippines
        rules.put("66",  new PhoneRule(new int[]{9}, new String[]{"6","8","9"}));           // Thailand
        rules.put("84",  new PhoneRule(new int[]{9}, new String[]{"3","5","7","8","9"}));   // Vietnam
        rules.put("95",  new PhoneRule(new int[]{9}, new String[]{"9"}));                   // Myanmar
        rules.put("855", new PhoneRule(new int[]{8,9}, new String[]{"1","6","7","8","9"})); // Cambodia
        rules.put("976", new PhoneRule(new int[]{8}, new String[]{"6","8","9"}));           // Mongolia
        // Europe
        rules.put("44",  new PhoneRule(new int[]{10}, new String[]{"7"}));                  // UK
        rules.put("33",  new PhoneRule(new int[]{9}, new String[]{"6","7"}));               // France
        rules.put("49",  new PhoneRule(new int[]{10,11}, new String[]{"1"}));               // Germany
        rules.put("39",  new PhoneRule(new int[]{10}, new String[]{"3"}));                  // Italy
        rules.put("34",  new PhoneRule(new int[]{9}, new String[]{"6","7"}));               // Spain
        rules.put("31",  new PhoneRule(new int[]{9}, new String[]{"6"}));                   // Netherlands
        rules.put("32",  new PhoneRule(new int[]{9}, null));                                // Belgium
        rules.put("41",  new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Switzerland
        rules.put("43",  new PhoneRule(new int[]{10}, new String[]{"6"}));                  // Austria
        rules.put("47",  new PhoneRule(new int[]{8}, new String[]{"4","9"}));               // Norway
        rules.put("48",  new PhoneRule(new int[]{9}, null));                                // Poland
        rules.put("30",  new PhoneRule(new int[]{10}, new String[]{"6"}));                  // Greece
        rules.put("420", new PhoneRule(new int[]{9}, new String[]{"6","7"}));               // Czech Republic
        rules.put("46",  new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Sweden
        rules.put("45",  new PhoneRule(new int[]{8}, null));                                // Denmark
        rules.put("40",  new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Romania
        rules.put("36",  new PhoneRule(new int[]{9}, null));                                // Hungary
        rules.put("380", new PhoneRule(new int[]{9}, null));                                // Ukraine
        // Americas
        rules.put("1",   new PhoneRule(new int[]{10}, null));                               // USA/Canada
        rules.put("52",  new PhoneRule(new int[]{10}, null));                               // Mexico
        rules.put("55",  new PhoneRule(new int[]{11}, null));                               // Brazil
        rules.put("57",  new PhoneRule(new int[]{10}, new String[]{"3"}));                  // Colombia
        rules.put("54",  new PhoneRule(new int[]{10}, new String[]{"9"}));                  // Argentina
        rules.put("56",  new PhoneRule(new int[]{9}, new String[]{"9"}));                   // Chile
        rules.put("58",  new PhoneRule(new int[]{10}, new String[]{"4"}));                  // Venezuela
        rules.put("51",  new PhoneRule(new int[]{9}, new String[]{"9"}));                   // Peru
        rules.put("593", new PhoneRule(new int[]{9}, new String[]{"9"}));                   // Ecuador
        rules.put("53",  new PhoneRule(new int[]{8}, new String[]{"5","6"}));               // Cuba
        // Africa
        rules.put("27",  new PhoneRule(new int[]{9}, new String[]{"6","7","8"}));           // South Africa
        rules.put("234", new PhoneRule(new int[]{10}, new String[]{"7","8","9"}));          // Nigeria
        rules.put("254", new PhoneRule(new int[]{9}, new String[]{"1","7"}));               // Kenya
        rules.put("233", new PhoneRule(new int[]{9}, new String[]{"2","5"}));               // Ghana
        rules.put("251", new PhoneRule(new int[]{9}, new String[]{"7","9"}));               // Ethiopia
        rules.put("255", new PhoneRule(new int[]{9}, new String[]{"6","7"}));               // Tanzania
        rules.put("256", new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Uganda
        rules.put("237", new PhoneRule(new int[]{9}, new String[]{"6"}));                   // Cameroon
        rules.put("225", new PhoneRule(new int[]{10}, null));                               // Ivory Coast
        rules.put("221", new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Senegal
        rules.put("252", new PhoneRule(new int[]{9}, new String[]{"6","7"}));               // Somalia
        rules.put("250", new PhoneRule(new int[]{9}, new String[]{"7"}));                   // Rwanda
        // Oceania
        rules.put("61",  new PhoneRule(new int[]{9}, new String[]{"4"}));                   // Australia
        rules.put("64",  new PhoneRule(new int[]{8,9,10}, new String[]{"2"}));              // New Zealand

        PHONE_RULES = Collections.unmodifiableMap(rules);

        Map<String, String> names = new HashMap<String, String>();
        names.put("965", "Kuwait");
        names.put("966", "Saudi Arabia");
        names.put("971", "UAE");
        names.put("973", "Bahrain");
        names.put("974", "Qatar");
        names.put("968", "Oman");
        names.put("962", "Jordan");
        names.put("961", "Lebanon");
        names.put("970", "Palestine");
        names.put("964", "Iraq");
        names.put("963", "Syria");
        names.put("967", "Yemen");
        names.put("98", "Iran");
        names.put("90", "Turkey");
        names.put("972", "Israel");
        names.put("20", "Egypt");
        names.put("218", "Libya");
        names.put("216", "Tunisia");
        names.put("212", "Morocco");
        names.put("213", "Algeria");
        names.put("249", "Sudan");
        names.put("91", "India");
        names.put("92", "Pakistan");
        names.put("880", "Bangladesh");
        names.put("94", "Sri Lanka");
        names.put("960", "Maldives");
        names.put("86", "China");
        names.put("81", "Japan");
        names.put("82", "South Korea");
        names.put("886", "Taiwan");
        names.put("65", "Singapore");
        names.put("60", "Malaysia");
        names.put("62", "Indonesia");
        names.put("63", "Philippines");
        names.put("66", "Thailand");
        names.put("84", "Vietnam");
        names.put("95", "Myanmar");
        names.put("855", "Cambodia");
        names.put("976", "Mongolia");
        names.put("44", "UK");
        names.put("33", "France");
        names.put("49", "Germany");
        names.put("39", "Italy");
        names.put("34", "Spain");
        names.put("31", "Netherlands");
        names.put("32", "Belgium");
        names.put("41", "Switzerland");
        names.put("43", "Austria");
        names.put("47", "Norway");
        names.put("48", "Poland");
        names.put("30", "Greece");
        names.put("420", "Czech Republic");
        names.put("46", "Sweden");
        names.put("45", "Denmark");
        names.put("40", "Romania");
        names.put("36", "Hungary");
        names.put("380", "Ukraine");
        names.put("1", "USA/Canada");
        names.put("52", "Mexico");
        names.put("55", "Brazil");
        names.put("57", "Colombia");
        names.put("54", "Argentina");
        names.put("56", "Chile");
        names.put("58", "Venezuela");
        names.put("51", "Peru");
        names.put("593", "Ecuador");
        names.put("53", "Cuba");
        names.put("27", "South Africa");
        names.put("234", "Nigeria");
        names.put("254", "Kenya");
        names.put("233", "Ghana");
        names.put("251", "Ethiopia");
        names.put("255", "Tanzania");
        names.put("256", "Uganda");
        names.put("237", "Cameroon");
        names.put("225", "Ivory Coast");
        names.put("221", "Senegal");
        names.put("252", "Somalia");
        names.put("250", "Rwanda");
        names.put("61", "Australia");
        names.put("64", "New Zealand");

        COUNTRY_NAMES = Collections.unmodifiableMap(names);
    }

    /**
     * Normalize a phone number to kwtSMS-accepted format (digits only, international format).
     *
     * 1. Converts Arabic-Indic and Extended Arabic-Indic digits to Latin
     * 2. Strips all non-digit characters
     * 3. Strips leading zeros
     * 4. Strips domestic trunk prefix (leading 0 after country code, e.g. 9660559... becomes 966559...)
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
        String normalized = start >= sb.length() ? "" : sb.substring(start);

        // Strip domestic trunk prefix (leading 0 after country code)
        // e.g. 9660559... -> 966559..., 97105x -> 9715x, 20010x -> 2010x
        String cc = findCountryCode(normalized);
        if (cc != null) {
            String local = normalized.substring(cc.length());
            if (local.startsWith("0")) {
                // Strip leading zeros from local part
                int localStart = 0;
                while (localStart < local.length() && local.charAt(localStart) == '0') {
                    localStart++;
                }
                normalized = cc + local.substring(localStart);
            }
        }

        return normalized;
    }

    /**
     * Find the country code prefix from a normalized phone number.
     * Tries 3-digit codes first, then 2-digit, then 1-digit (longest match wins).
     *
     * @param normalized digits-only phone number
     * @return country code string or null if not found
     */
    public static String findCountryCode(String normalized) {
        if (normalized == null) return null;
        if (normalized.length() >= 3) {
            String cc3 = normalized.substring(0, 3);
            if (PHONE_RULES.containsKey(cc3)) return cc3;
        }
        if (normalized.length() >= 2) {
            String cc2 = normalized.substring(0, 2);
            if (PHONE_RULES.containsKey(cc2)) return cc2;
        }
        if (normalized.length() >= 1) {
            String cc1 = normalized.substring(0, 1);
            if (PHONE_RULES.containsKey(cc1)) return cc1;
        }
        return null;
    }

    /**
     * Validate a normalized phone number against country-specific format rules.
     * Checks local number length and mobile starting digits.
     * Numbers with no matching country rules pass through (generic E.164 only).
     *
     * @param normalized digits-only phone number
     * @return ValidationResult with valid flag and optional error
     */
    public static ValidationResult validatePhoneFormat(String normalized) {
        String cc = findCountryCode(normalized);
        if (cc == null) return new ValidationResult(true, null, normalized);

        PhoneRule rule = PHONE_RULES.get(cc);
        String local = normalized.substring(cc.length());
        String country = COUNTRY_NAMES.containsKey(cc) ? COUNTRY_NAMES.get(cc) : "+" + cc;

        // Check local number length
        boolean lengthOk = false;
        for (int len : rule.localLengths) {
            if (local.length() == len) {
                lengthOk = true;
                break;
            }
        }
        if (!lengthOk) {
            StringBuilder expected = new StringBuilder();
            for (int i = 0; i < rule.localLengths.length; i++) {
                if (i > 0) expected.append(" or ");
                expected.append(rule.localLengths[i]);
            }
            return new ValidationResult(false,
                    "Invalid " + country + " number: expected " + expected + " digits after +" + cc + ", got " + local.length(),
                    normalized);
        }

        // Check mobile starting digits (if rules exist for this country)
        if (rule.mobileStartDigits != null && rule.mobileStartDigits.length > 0 && local.length() > 0) {
            boolean prefixOk = false;
            for (String prefix : rule.mobileStartDigits) {
                if (local.startsWith(prefix)) {
                    prefixOk = true;
                    break;
                }
            }
            if (!prefixOk) {
                StringBuilder prefixes = new StringBuilder();
                for (int i = 0; i < rule.mobileStartDigits.length; i++) {
                    if (i > 0) prefixes.append(", ");
                    prefixes.append(rule.mobileStartDigits[i]);
                }
                return new ValidationResult(false,
                        "Invalid " + country + " mobile number: after +" + cc + " must start with " + prefixes,
                        normalized);
            }
        }

        return new ValidationResult(true, null, normalized);
    }

    /**
     * Validate a phone number input before sending to the API.
     *
     * @param phone raw phone number input
     * @return a ValidationResult with valid flag, error message, and normalized number
     */
    public static ValidationResult validatePhoneInput(String phone) {
        String raw = phone == null ? "" : phone;
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

        // Country-specific format validation
        ValidationResult formatCheck = validatePhoneFormat(normalized);
        if (!formatCheck.isValid()) {
            return formatCheck;
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
        return new ArrayList<String>(new LinkedHashSet<String>(phones));
    }

    /**
     * Country-specific phone number validation rule.
     */
    static final class PhoneRule {
        final int[] localLengths;
        final String[] mobileStartDigits;

        PhoneRule(int[] localLengths, String[] mobileStartDigits) {
            this.localLengths = localLengths;
            this.mobileStartDigits = mobileStartDigits;
        }
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
