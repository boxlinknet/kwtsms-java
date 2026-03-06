package com.kwtsms;

/**
 * Single entry in a delivery report.
 */
public final class DeliveryReportEntry {
    private final String number;
    private final String status;

    public DeliveryReportEntry(String number, String status) {
        this.number = number;
        this.status = status;
    }

    public String getNumber() { return number; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "DeliveryReportEntry{number='" + number + "', status='" + status + "'}";
    }
}
