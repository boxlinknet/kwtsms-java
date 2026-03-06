package com.kwtsms;

import java.util.Collections;
import java.util.List;

/**
 * Result of senderIds() call.
 */
public final class SenderIdResult {
    private final String result;
    private final List<String> senderIds;
    private final String code;
    private final String description;
    private final String action;

    public SenderIdResult(String result, List<String> senderIds, String code,
                          String description, String action) {
        this.result = result;
        this.senderIds = senderIds == null ? Collections.emptyList() : Collections.unmodifiableList(senderIds);
        this.code = code;
        this.description = description;
        this.action = action;
    }

    public String getResult() { return result; }
    public List<String> getSenderIds() { return senderIds; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getAction() { return action; }

    @Override
    public String toString() {
        return "SenderIdResult{result='" + result + "', senderIds=" + senderIds + "}";
    }
}
