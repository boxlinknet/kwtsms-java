package com.kwtsms;

import java.util.Collections;
import java.util.List;

/**
 * Result of a single send() call.
 */
public final class SendResult {
    private final String result;
    private final String msgId;
    private final Integer numbers;
    private final Integer pointsCharged;
    private final Double balanceAfter;
    private final Long unixTimestamp;
    private final String code;
    private final String description;
    private final String action;
    private final List<InvalidEntry> invalid;

    public SendResult(String result, String msgId, Integer numbers, Integer pointsCharged,
                      Double balanceAfter, Long unixTimestamp, String code, String description,
                      String action, List<InvalidEntry> invalid) {
        this.result = result;
        this.msgId = msgId;
        this.numbers = numbers;
        this.pointsCharged = pointsCharged;
        this.balanceAfter = balanceAfter;
        this.unixTimestamp = unixTimestamp;
        this.code = code;
        this.description = description;
        this.action = action;
        this.invalid = invalid == null ? Collections.emptyList() : Collections.unmodifiableList(invalid);
    }

    public String getResult() { return result; }
    public String getMsgId() { return msgId; }
    public Integer getNumbers() { return numbers; }
    public Integer getPointsCharged() { return pointsCharged; }
    public Double getBalanceAfter() { return balanceAfter; }
    public Long getUnixTimestamp() { return unixTimestamp; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getAction() { return action; }
    public List<InvalidEntry> getInvalid() { return invalid; }

    @Override
    public String toString() {
        return "SendResult{result='" + result + "', msgId='" + msgId + "', numbers=" + numbers +
               ", pointsCharged=" + pointsCharged + ", balanceAfter=" + balanceAfter +
               ", code='" + code + "', description='" + description + "'}";
    }
}
