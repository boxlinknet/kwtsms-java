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
        return (input == null ? that.input == null : input.equals(that.input))
            && (error == null ? that.error == null : error.equals(that.error));
    }

    @Override
    public int hashCode() {
        int h = input == null ? 0 : input.hashCode();
        return 31 * h + (error == null ? 0 : error.hashCode());
    }
}
