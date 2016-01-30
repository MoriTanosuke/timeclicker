package de.kopis.timeclicker.model;

import java.util.Date;

public class TimeSumWithDate extends TimeSum {
    private Date date;

    public TimeSumWithDate(Date date, long duration) {
        super(duration);
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
