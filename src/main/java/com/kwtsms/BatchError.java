package com.kwtsms;

/**
 * Per-batch error in bulk send.
 */
public final class BatchError {
    private final int batch;
    private final String code;
    private final String description;

    public BatchError(int batch, String code, String description) {
        this.batch = batch;
        this.code = code;
        this.description = description;
    }

    public int getBatch() { return batch; }
    public String getCode() { return code; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "BatchError{batch=" + batch + ", code='" + code + "', description='" + description + "'}";
    }
}
