package com.kwtsms;

/**
 * Result of status() call.
 */
public final class StatusResult {
    private final String result;
    private final String status;
    private final String statusDescription;
    private final String code;
    private final String description;
    private final String action;

    public StatusResult(String result, String status, String statusDescription,
                        String code, String description, String action) {
        this.result = result;
        this.status = status;
        this.statusDescription = statusDescription;
        this.code = code;
        this.description = description;
        this.action = action;
    }

    public String getResult() { return result; }
    public String getStatus() { return status; }
    public String getStatusDescription() { return statusDescription; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getAction() { return action; }

    @Override
    public String toString() {
        return "StatusResult{result='" + result + "', status='" + status + "'}";
    }
}
