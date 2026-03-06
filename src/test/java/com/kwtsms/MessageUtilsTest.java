package com.kwtsms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageUtilsTest {

    @Test
    void cleanMessage_plainTextUnchanged() {
        assertEquals("Hello World", MessageUtils.cleanMessage("Hello World"));
    }

    @Test
    void cleanMessage_convertsArabicIndicDigits() {
        assertEquals("OTP: 1234", MessageUtils.cleanMessage("OTP: \u0661\u0662\u0663\u0664"));
    }

    @Test
    void cleanMessage_convertsExtendedArabicIndicDigits() {
        assertEquals("Code: 5678", MessageUtils.cleanMessage("Code: \u06F5\u06F6\u06F7\u06F8"));
    }

    @Test
    void cleanMessage_stripsEmoji() {
        assertEquals("Hello ", MessageUtils.cleanMessage("Hello \uD83D\uDE00"));
    }

    @Test
    void cleanMessage_stripsMultipleEmojis() {
        assertEquals("Test", MessageUtils.cleanMessage("\uD83D\uDE00Test\uD83D\uDE01\uD83D\uDE02"));
    }

    @Test
    void cleanMessage_stripsZeroWidthSpace() {
        assertEquals("Hello", MessageUtils.cleanMessage("He\u200Bllo"));
    }

    @Test
    void cleanMessage_stripsBOM() {
        assertEquals("Hello", MessageUtils.cleanMessage("\uFEFFHello"));
    }

    @Test
    void cleanMessage_stripsSoftHyphen() {
        assertEquals("Hello", MessageUtils.cleanMessage("Hel\u00ADlo"));
    }

    @Test
    void cleanMessage_stripsZeroWidthNonJoiner() {
        assertEquals("Hello", MessageUtils.cleanMessage("He\u200Cllo"));
    }

    @Test
    void cleanMessage_stripsZeroWidthJoiner() {
        assertEquals("Hello", MessageUtils.cleanMessage("He\u200Dllo"));
    }

    @Test
    void cleanMessage_stripsWordJoiner() {
        assertEquals("Hello", MessageUtils.cleanMessage("He\u2060llo"));
    }

    @Test
    void cleanMessage_stripsObjectReplacement() {
        assertEquals("Hello", MessageUtils.cleanMessage("He\uFFFCllo"));
    }

    @Test
    void cleanMessage_stripsDirectionalMarks() {
        assertEquals("Hello", MessageUtils.cleanMessage("He\u200Ello\u200F"));
    }

    @Test
    void cleanMessage_stripsDirectionalFormatting() {
        assertEquals("Hello", MessageUtils.cleanMessage("\u202AHe\u202Bllo\u202C"));
    }

    @Test
    void cleanMessage_stripsDirectionalIsolates() {
        assertEquals("Hello", MessageUtils.cleanMessage("\u2066He\u2067llo\u2069"));
    }

    @Test
    void cleanMessage_stripsC0ControlChars() {
        // \u0001 (SOH) should be stripped, \n and \t preserved
        assertEquals("He\nllo\tWorld", MessageUtils.cleanMessage("He\u0001\nllo\tWorld"));
    }

    @Test
    void cleanMessage_stripsDEL() {
        assertEquals("Hello", MessageUtils.cleanMessage("He\u007Fllo"));
    }

    @Test
    void cleanMessage_stripsC1ControlChars() {
        assertEquals("Hello", MessageUtils.cleanMessage("He\u0080llo"));
    }

    @Test
    void cleanMessage_stripsHtmlTags() {
        assertEquals("Hello World", MessageUtils.cleanMessage("<b>Hello</b> <i>World</i>"));
    }

    @Test
    void cleanMessage_stripsComplexHtml() {
        assertEquals("Click here", MessageUtils.cleanMessage("<a href=\"https://example.com\">Click here</a>"));
    }

    @Test
    void cleanMessage_preservesArabicText() {
        String arabic = "\u0645\u0631\u062D\u0628\u0627";
        assertEquals(arabic, MessageUtils.cleanMessage(arabic));
    }

    @Test
    void cleanMessage_preservesNewlines() {
        assertEquals("Line1\nLine2", MessageUtils.cleanMessage("Line1\nLine2"));
    }

    @Test
    void cleanMessage_preservesTabs() {
        assertEquals("Col1\tCol2", MessageUtils.cleanMessage("Col1\tCol2"));
    }

    @Test
    void cleanMessage_nullInput() {
        assertEquals("", MessageUtils.cleanMessage(null));
    }

    @Test
    void cleanMessage_emptyInput() {
        assertEquals("", MessageUtils.cleanMessage(""));
    }

    @Test
    void cleanMessage_combinedCleaning() {
        String input = "\uFEFF<b>\u0661\u0662\u0663</b> \uD83D\uDE00\u200BTest\u0001";
        String expected = "123 Test";
        assertEquals(expected, MessageUtils.cleanMessage(input));
    }

    @Test
    void cleanMessage_variationSelectors() {
        assertEquals("Star", MessageUtils.cleanMessage("Star\uFE0F"));
    }

    @Test
    void cleanMessage_combiningEnclosingKeycap() {
        assertEquals("1", MessageUtils.cleanMessage("1\u20E3"));
    }
}
