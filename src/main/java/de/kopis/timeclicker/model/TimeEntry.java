package de.kopis.timeclicker.model;

import java.io.Serializable;
import java.util.Date;

public class TimeEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date start = null;
    private Date stop = null;
    private String key;
    private String tags;

    public TimeEntry() {
        this(new Date());
    }

    public TimeEntry(Date start, Date stop) {
        this.start = start;
        this.stop = stop;
    }

    public TimeEntry(Date start) {
        this.start = start;
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

    public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Override
    public String toString() {
        return "TimeEntry{" +
                "start=" + start +
                ", stop=" + stop +
                ", key='" + key + "'" +
                ", tags='" + tags + "'" + 
                '}';
    }
}
