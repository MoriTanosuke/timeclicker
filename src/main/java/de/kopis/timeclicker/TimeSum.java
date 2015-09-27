package de.kopis.timeclicker;

public class TimeSum {
    private long duration;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "TimeSum{" +
                "duration=" + duration +
                '}';
    }
}
