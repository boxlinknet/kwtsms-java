package com.kwtsms;

import java.util.Collections;
import java.util.List;

/**
 * Result of coverage() call.
 */
public final class CoverageResult {
    private final String result;
    private final List<String> prefixes;
    private final String code;
    private final String description;
    private final String action;

    public CoverageResult(String result, List<String> prefixes, String code,
                          String description, String action) {
        this.result = result;
        this.prefixes = prefixes == null ? Collections.emptyList() : Collections.unmodifiableList(prefixes);
        this.code = code;
        this.description = description;
        this.action = action;
    }

    public String getResult() { return result; }
    public List<String> getPrefixes() { return prefixes; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getAction() { return action; }

    @Override
    public String toString() {
        return "CoverageResult{result='" + result + "', prefixes=" + prefixes + "}";
    }
}
