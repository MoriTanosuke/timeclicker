package de.kopis.timeclicker.model;

import java.io.Serializable;
import java.util.Date;

public class TimeEntry implements Serializable {
    private static final long serialVersionUID = 1L;

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
