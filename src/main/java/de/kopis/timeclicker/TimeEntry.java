package de.kopis.timeclicker;

import java.util.Date;

public class TimeEntry {

    private Date start = null;
    private Date stop = null;
    private String key;

    public TimeEntry() {
        start = new Date();
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStop() {
        return stop;
    }

    public void setStop(Date stop) {
        this.stop = stop;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
