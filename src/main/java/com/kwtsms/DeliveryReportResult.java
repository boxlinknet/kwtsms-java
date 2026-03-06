package com.kwtsms;

import java.util.Collections;
import java.util.List;

/**
 * Result of deliveryReport() call.
 */
public final class DeliveryReportResult {
    private final String result;
    private final List<DeliveryReportEntry> report;
    private final String code;
    private final String description;
    private final String action;

    public DeliveryReportResult(String result, List<DeliveryReportEntry> report,
                                String code, String description, String action) {
        this.result = result;
        this.report = report == null ? Collections.emptyList() : Collections.unmodifiableList(report);
        this.code = code;
        this.description = description;
        this.action = action;
    }

    public String getResult() { return result; }
    public List<DeliveryReportEntry> getReport() { return report; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getAction() { return action; }

    @Override
    public String toString() {
        return "DeliveryReportResult{result='" + result + "', report=" + report.size() + " entries}";
    }
}
