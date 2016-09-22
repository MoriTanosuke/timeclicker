package de.kopis.timeclicker.model;

import java.io.Serializable;
import java.util.TimeZone;

public class UserSettings implements Serializable {
    public static final int HOURS_PER_DAY = 8;
    public static final int HOURS_PER_DAY_IN_MILLISECONDS = HOURS_PER_DAY * 60 * 60 * 1000;

    private long workingDurationPerDay = HOURS_PER_DAY_IN_MILLISECONDS;
    private TimeZone timezone = TimeZone.getDefault();
    private String key;

    public long getWorkingDurationPerDay() {
        return workingDurationPerDay;
    }

    public void setWorkingDurationPerDay(long workingDurationPerDay) {
        this.workingDurationPerDay = workingDurationPerDay;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
