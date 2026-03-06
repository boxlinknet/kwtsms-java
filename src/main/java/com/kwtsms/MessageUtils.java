package com.kwtsms;

/**
 * Message cleaning utilities for kwtSMS.
 * Strips emojis, hidden characters, control chars, and HTML tags.
 * Converts Arabic-Indic digits to Latin. Preserves Arabic text.
 */
public final class MessageUtils {

    private MessageUtils() {}

    /**
     * Clean a message for safe sending via kwtSMS.
     *
     * Processing order:
     * 1. Convert Arabic-Indic and Extended Arabic-Indic digits to Latin
     * 2. Remove emojis
     * 3. Remove hidden invisible characters (zero-width space, BOM, soft hyphen, etc.)
     * 4. Remove directional formatting characters
     * 5. Remove C0 and C1 control characters (preserve newline and tab)
     * 6. Strip HTML tags
     *
     * @param text the raw message text
     * @return cleaned message safe for the kwtSMS API
     */
    public static String cleanMessage(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(text.length());

        // Steps 1-5: character-level processing using code points
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            int charCount = Character.charCount(cp);

            if (cp >= 0x0660 && cp <= 0x0669) {
                // Step 1a: Arabic-Indic digits -> Latin
                sb.append((char) ('0' + (cp - 0x0660)));
            } else if (cp >= 0x06F0 && cp <= 0x06F9) {
                // Step 1b: Extended Arabic-Indic / Persian digits -> Latin
                sb.append((char) ('0' + (cp - 0x06F0)));
            } else if (isEmoji(cp)) {
                // Step 2: skip emojis
            } else if (isHiddenInvisible(cp)) {
                // Step 3: skip hidden invisible chars
            } else if (isDirectionalFormatting(cp)) {
                // Step 4: skip directional formatting
            } else if (isControlChar(cp)) {
                // Step 5: skip control chars
            } else {
                // Keep everything else (including Arabic text)
                sb.appendCodePoint(cp);
            }

            i += charCount;
        }

        // Step 6: Strip HTML tags
        return stripHtmlTags(sb.toString());
    }

    private static boolean isEmoji(int cp) {
        return (cp >= 0x1F000 && cp <= 0x1F02F) ||   // Mahjong, domino tiles
               (cp >= 0x1F0A0 && cp <= 0x1F0FF) ||   // Playing cards
               (cp >= 0x1F1E0 && cp <= 0x1F1FF) ||   // Regional indicator symbols / flags
               (cp >= 0x1F300 && cp <= 0x1F5FF) ||   // Misc symbols and pictographs
               (cp >= 0x1F600 && cp <= 0x1F64F) ||   // Emoticons
               (cp >= 0x1F680 && cp <= 0x1F6FF) ||   // Transport and map
               (cp >= 0x1F700 && cp <= 0x1F77F) ||   // Alchemical symbols
               (cp >= 0x1F780 && cp <= 0x1F7FF) ||   // Geometric shapes extended
               (cp >= 0x1F800 && cp <= 0x1F8FF) ||   // Supplemental arrows
               (cp >= 0x1F900 && cp <= 0x1F9FF) ||   // Supplemental symbols and pictographs
               (cp >= 0x1FA00 && cp <= 0x1FA6F) ||   // Chess symbols
               (cp >= 0x1FA70 && cp <= 0x1FAFF) ||   // Symbols and pictographs extended
               (cp >= 0x2600 && cp <= 0x26FF) ||     // Misc symbols
               (cp >= 0x2700 && cp <= 0x27BF) ||     // Dingbats
               (cp >= 0xFE00 && cp <= 0xFE0F) ||     // Variation selectors
               cp == 0x20E3 ||                        // Combining enclosing keycap
               (cp >= 0xE0000 && cp <= 0xE007F);     // Tags block
    }

    private static boolean isHiddenInvisible(int cp) {
        return cp == 0x200B || // Zero-width space
               cp == 0x200C || // Zero-width non-joiner
               cp == 0x200D || // Zero-width joiner
               cp == 0x2060 || // Word joiner
               cp == 0x00AD || // Soft hyphen
               cp == 0xFEFF || // BOM
               cp == 0xFFFC;   // Object replacement character
    }

    private static boolean isDirectionalFormatting(int cp) {
        return cp == 0x200E ||                    // Left-to-right mark
               cp == 0x200F ||                    // Right-to-left mark
               (cp >= 0x202A && cp <= 0x202E) ||  // LRE, RLE, PDF, LRO, RLO
               (cp >= 0x2066 && cp <= 0x2069);    // LRI, RLI, FSI, PDI
    }

    private static boolean isControlChar(int cp) {
        // C0 controls (0x0000-0x001F) except TAB (0x0009) and LF (0x000A)
        if (cp >= 0x0000 && cp <= 0x001F && cp != 0x0009 && cp != 0x000A) return true;
        // DEL
        if (cp == 0x007F) return true;
        // C1 controls
        return cp >= 0x0080 && cp <= 0x009F;
    }

    private static String stripHtmlTags(String text) {
        return text.replaceAll("<[^>]*>", "");
    }
}
