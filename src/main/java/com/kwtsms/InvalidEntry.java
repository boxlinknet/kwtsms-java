package com.kwtsms;

/**
 * Entry representing a phone number that failed local validation.
 */
public final class InvalidEntry {
    private final String input;
    private final String error;

    public InvalidEntry(String input, String error) {
        this.input = input;
        this.error = error;
    }

    public String getInput() { return input; }
    public String getError() { return error; }

    @Override
    public String toString() {
        return "InvalidEntry{input='" + input + "', error='" + error + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvalidEntry)) return false;
        InvalidEntry that = (InvalidEntry) o;
        return input.equals(that.input) && error.equals(that.error);
    }

    @Override
    public int hashCode() {
        return 31 * input.hashCode() + error.hashCode();
    }
}
