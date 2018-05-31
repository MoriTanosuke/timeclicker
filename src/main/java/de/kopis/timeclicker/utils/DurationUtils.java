package de.kopis.timeclicker.utils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.util.logging.Logger;

public class DurationUtils {
    private static final Logger LOGGER = Logger.getLogger(DurationUtils.class.getName());

    public static String getReadableDuration(long duration) {
        String readableDuration = "" + duration;
        try {
            Duration d = DatatypeFactory.newInstance().newDuration(duration);
            readableDuration = String.format("%02d:%02d:%02d", d.getDays() * 24 + d.getHours(),
                    d.getMinutes(), d.getSeconds());
        } catch (DatatypeConfigurationException e) {
            LOGGER.severe("Can not format duration: " + e.getMessage());
        }
        // add prefix if negative duration
        if (duration < 0) {
            readableDuration = "-" + readableDuration;
        }
        LOGGER.fine("readable duration: " + readableDuration);
        return readableDuration;
    }
}
