package de.kopis.timeclicker.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.TimeZone;

public class UserSettings implements Serializable {
    public static final int HOURS_PER_DAY = 8;
    public static final String BREAK_DURATION_PER_DAY = "breakDurationPerDay";
    public static final String WORKING_DURATION_PER_DAY = "workingDurationPerDay";
    public static final String TIMEZONE = "timezone";
    public static final String COUNTRY = "country";
    public static final String LANGUAGE = "language";
    public static final String VARIANT = "variant";
    public static final String USER_ID = "userId";

    private Duration workingDurationPerDay = Duration.of(HOURS_PER_DAY, ChronoUnit.HOURS);
    private Duration breakDurationPerDay = Duration.ZERO;
    private TimeZone timezone = TimeZone.getDefault();
    private Locale locale = Locale.getDefault();
    private String key;

    public Duration getWorkingDurationPerDay() {
        return workingDurationPerDay;
    }

    public void setWorkingDurationPerDay(Duration workingDurationPerDay) {
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

    public void setBreakDurationPerDay(Duration breakDurationPerDay) {
        this.breakDurationPerDay = breakDurationPerDay;
    }

    public Duration getBreakDurationPerDay() {
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
