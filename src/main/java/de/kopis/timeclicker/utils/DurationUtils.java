package de.kopis.timeclicker.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class DurationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DurationUtils.class);

    public static String getReadableDuration(long duration) {
        String readableDuration = "" + duration;
        try {
            Duration d = DatatypeFactory.newInstance().newDuration(duration);
            readableDuration = String.format("%02d:%02d:%02d", d.getDays() * 24 + d.getHours(),
                    d.getMinutes(), d.getSeconds());
        } catch (DatatypeConfigurationException e) {
            LOGGER.warn("Can not format duration: " + e.getMessage());
        }
        // add prefix if negative duration
        if (duration < 0) {
            readableDuration = "-" + readableDuration;
        }
        LOGGER.debug("readable duration: " + readableDuration);
        return readableDuration;
    }
}
