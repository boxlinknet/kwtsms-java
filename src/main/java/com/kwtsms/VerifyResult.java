package com.kwtsms;

/**
 * Result of verify() call.
 */
public final class VerifyResult {
    private final boolean ok;
    private final Double balance;
    private final String error;

    public VerifyResult(boolean ok, Double balance, String error) {
        this.ok = ok;
        this.balance = balance;
        this.error = error;
    }

    public boolean isOk() { return ok; }
    public Double getBalance() { return balance; }
    public String getError() { return error; }

    @Override
    public String toString() {
        return "VerifyResult{ok=" + ok + ", balance=" + balance + ", error='" + error + "'}";
    }
}
