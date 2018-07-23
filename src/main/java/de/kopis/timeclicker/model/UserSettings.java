package de.kopis.timeclicker.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

public class UserSettings implements Serializable {
    public static final int HOURS_PER_DAY = 8;
    public static final int HOURS_PER_DAY_IN_MILLISECONDS = HOURS_PER_DAY * 60 * 60 * 1000;
    public static final String BREAK_DURATION_PER_DAY = "breakDurationPerDay";
    public static final String WORKING_DURATION_PER_DAY = "workingDurationPerDay";
    public static final String TIMEZONE = "timezone";
    public static final String COUNTRY = "country";
    public static final String LANGUAGE = "language";
    public static final String VARIANT = "variant";
    public static final String USER_ID = "userId";

    private long workingDurationPerDay = HOURS_PER_DAY_IN_MILLISECONDS;
    private long breakDurationPerDay = 0;
    private TimeZone timezone = TimeZone.getDefault();
    private Locale locale = Locale.getDefault();
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

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setBreakDurationPerDay(long breakDurationPerDay) {
        this.breakDurationPerDay = breakDurationPerDay;
    }

    public long getBreakDurationPerDay() {
        return breakDurationPerDay;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "workingDurationPerDay=" + workingDurationPerDay +
                ", breakDurationPerDay=" + breakDurationPerDay +
                ", timezone=" + timezone.getDisplayName() +
                ", locale=" + locale.getDisplayName() +
                ", key='" + key + '\'' +
                '}';
    }
}
