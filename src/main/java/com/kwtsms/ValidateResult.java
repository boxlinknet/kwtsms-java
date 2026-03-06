package com.kwtsms;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result of validate() call.
 */
public final class ValidateResult {
    private final List<String> ok;
    private final List<String> er;
    private final List<String> nr;
    private final List<InvalidEntry> rejected;
    private final String error;
    private final Map<String, Object> raw;

    public ValidateResult(List<String> ok, List<String> er, List<String> nr,
                          List<InvalidEntry> rejected, String error, Map<String, Object> raw) {
        this.ok = ok == null ? Collections.emptyList() : Collections.unmodifiableList(ok);
        this.er = er == null ? Collections.emptyList() : Collections.unmodifiableList(er);
        this.nr = nr == null ? Collections.emptyList() : Collections.unmodifiableList(nr);
        this.rejected = rejected == null ? Collections.emptyList() : Collections.unmodifiableList(rejected);
        this.error = error;
        this.raw = raw == null ? Collections.emptyMap() : Collections.unmodifiableMap(raw);
    }

    public List<String> getOk() { return ok; }
    public List<String> getEr() { return er; }
    public List<String> getNr() { return nr; }
    public List<InvalidEntry> getRejected() { return rejected; }
    public String getError() { return error; }
    public Map<String, Object> getRaw() { return raw; }

    @Override
    public String toString() {
        return "ValidateResult{ok=" + ok.size() + ", er=" + er.size() + ", nr=" + nr.size() +
               ", rejected=" + rejected.size() + ", error='" + error + "'}";
    }
}
