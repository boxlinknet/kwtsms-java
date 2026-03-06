package com.kwtsms;

import java.util.Collections;
import java.util.List;

/**
 * Result of a bulk send (>200 numbers, auto-batched).
 */
public final class BulkSendResult {
    private final String result;
    private final boolean bulk;
    private final int batches;
    private final int numbers;
    private final int pointsCharged;
    private final Double balanceAfter;
    private final List<String> msgIds;
    private final List<BatchError> errors;
    private final List<InvalidEntry> invalid;
    private final String code;
    private final String description;

    public BulkSendResult(String result, boolean bulk, int batches, int numbers, int pointsCharged,
                          Double balanceAfter, List<String> msgIds, List<BatchError> errors,
                          List<InvalidEntry> invalid, String code, String description) {
        this.result = result;
        this.bulk = bulk;
        this.batches = batches;
        this.numbers = numbers;
        this.pointsCharged = pointsCharged;
        this.balanceAfter = balanceAfter;
        this.msgIds = msgIds == null ? Collections.emptyList() : Collections.unmodifiableList(msgIds);
        this.errors = errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
        this.invalid = invalid == null ? Collections.emptyList() : Collections.unmodifiableList(invalid);
        this.code = code;
        this.description = description;
    }

    public String getResult() { return result; }
    public boolean isBulk() { return bulk; }
    public int getBatches() { return batches; }
    public int getNumbers() { return numbers; }
    public int getPointsCharged() { return pointsCharged; }
    public Double getBalanceAfter() { return balanceAfter; }
    public List<String> getMsgIds() { return msgIds; }
    public List<BatchError> getErrors() { return errors; }
    public List<InvalidEntry> getInvalid() { return invalid; }
    public String getCode() { return code; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "BulkSendResult{result='" + result + "', batches=" + batches +
               ", numbers=" + numbers + ", pointsCharged=" + pointsCharged + "}";
    }
}
