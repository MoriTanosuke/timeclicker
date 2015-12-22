package de.kopis.timeclicker.model;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

public class TimeSum implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(TimeSum.class.getName());

    private static final long serialVersionUID = 1L;
    private long duration;

    public TimeSum(long duration) {
        this.duration = duration;
    }

    public TimeSum(TimeEntry entry) {
        this.duration = calculateDuration(entry);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void addDuration(long duration) {
        this.duration += duration;
    }

    private long calculateDuration(TimeEntry entry) {
        final Date start = entry.getStart();
        // check if the entity is already stopped, else use the current date
        Date stop = entry.getStop();
        if (stop == null) {
            stop = new Date();
        }

        return stop.getTime() - start.getTime();
    }

    @Override
    public String toString() {
        return getReadableDuration(this);
    }


    private String getReadableDuration(TimeSum sum) {
        if (sum != null) {
            return getReadableDuration(sum.getDuration());
        } else {
            return "--";
        }
    }

    private String getReadableDuration(long duration) {
        // TODO use formatter with model

        String readableDuration = "" + duration;
        try {
            Duration d = DatatypeFactory.newInstance().newDuration(duration);
            readableDuration = String.format("%02d hours, %02d minutes, %02d seconds", d.getDays() * 24 + d.getHours(),
                    d.getMinutes(), d.getSeconds());
        } catch (DatatypeConfigurationException e) {
            LOGGER.severe("Can not format duration: " + e.getMessage());
        }
        return readableDuration;
    }
}
