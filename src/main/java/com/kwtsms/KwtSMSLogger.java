package com.kwtsms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSONL logger for API calls. Never throws, never blocks the main flow.
 */
final class KwtSMSLogger {

    private KwtSMSLogger() {}

    private static final Object WRITE_LOCK = new Object();

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Mask credentials in a request payload for logging.
     * Replaces the "password" value with "***".
     */
    static Map<String, String> maskCredentials(Map<String, Object> payload) {
        Map<String, String> masked = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if ("password".equals(entry.getKey()) || "username".equals(entry.getKey())) {
                masked.put(entry.getKey(), "***");
            } else {
                masked.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue().toString());
            }
        }
        return masked;
    }

    /**
     * Write a JSONL log entry. Never throws.
     */
    static void writeLog(String logFile, String endpoint, Map<String, Object> request,
                         String response, boolean ok, String error) {
        if (logFile == null || logFile.isEmpty()) return;

        try {
            String ts = Instant.now().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
            Map<String, String> masked = maskCredentials(request);

            StringBuilder sb = new StringBuilder();
            sb.append("{\"ts\":\"").append(escapeJson(ts)).append("\"");
            sb.append(",\"endpoint\":\"").append(escapeJson(endpoint)).append("\"");

            // request object
            sb.append(",\"request\":{");
            boolean first = true;
            for (Map.Entry<String, String> e : masked.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append("\"").append(escapeJson(e.getKey())).append("\":\"")
                  .append(escapeJson(e.getValue())).append("\"");
            }
            sb.append("}");

            sb.append(",\"response\":\"").append(escapeJson(response)).append("\"");
            sb.append(",\"ok\":").append(ok);
            if (error != null) {
                sb.append(",\"error\":\"").append(escapeJson(error)).append("\"");
            }
            sb.append("}\n");

            synchronized (WRITE_LOCK) {
                File file = new File(logFile);
                try (OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(file, true), StandardCharsets.UTF_8)) {
                    writer.write(sb.toString());
                }
            }
        } catch (Exception e) {
            // Logging must never crash the main flow
        }
    }

    static void writeLog(String logFile, String endpoint, Map<String, Object> request,
                         String response, boolean ok) {
        writeLog(logFile, endpoint, request, response, ok, null);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}
