package de.kopis.timeclicker.model;

import java.io.Serializable;

public class TimeSum implements Serializable {
    private static final long serialVersionUID = 1L;
    private long duration;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void addDuration(long duration) {
        this.duration += duration;
    }

    @Override
    public String toString() {
        return "TimeSum{" +
                "duration=" + duration +
                '}';
    }
}
