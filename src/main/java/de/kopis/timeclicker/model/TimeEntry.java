package de.kopis.timeclicker.model;


import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public class TimeEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ENTRY_USER_ID = "userId";
    public static final String ENTRY_START = "start";
    public static final String ENTRY_STOP = "stop";
    public static final String ENTRY_DESCRIPTION = "description";
    public static final String ENTRY_TAGS = "tags";
    public static final String ENTRY_PROJECT = "project";
    public static final String ENTRY_BREAK_DURATION = "breakDuration";

    // Supplier for default value of stop - used during testing
    protected Supplier<Instant> stopSupplier = () -> Instant.now();

    private Instant start = null;
    private Instant stop = null;
    private Duration breakDuration = Duration.of(0, ChronoUnit.SECONDS);
    private String key;
    private String tags;
    private String project;
    private String description;

    public TimeEntry() {
        this(Instant.now());
    }

    public TimeEntry(Instant start, Instant stop) {
        this.start = start;
        this.stop = stop;
    }

    public TimeEntry(Instant start) {
        this.start = start;
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getStop() {
        return stop;
    }

    public void setStop(Instant stop) {
        this.stop = stop;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Duration getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(Duration breakDuration) {
        this.breakDuration = breakDuration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Duration getDuration() {
        if (this.start == null && this.stop == null) return Duration.ZERO;
        if (this.start == null && this.stop != null)
            throw new IllegalArgumentException("start is null, stop is " + this.stop + "  - can not calculate duration");

        Instant t1 = this.start;
        Instant t2 = stopSupplier.get();
        if (this.stop != null) {
            t2 = this.stop;
        }
        return Duration.between(t1, t2).minus(breakDuration);
    }

    @Override
    public String toString() {
        return "TimeEntry{" +
                "start=" + start +
                ", stop=" + stop +
                ", break=" + breakDuration +
                ", key='" + key + "'" +
                ", tags='" + tags + "'" +
                ", project='" + project + "'" +
                '}';
    }
}
