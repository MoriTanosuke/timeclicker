package de.kopis.timeclicker.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DurationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DurationUtils.class);

    public static String getReadableDuration(Duration duration) {
        String readableDuration = formatDuration(duration);
        LOGGER.debug("readable duration: " + readableDuration);
        if (duration.isNegative()) {
            readableDuration = "-" + readableDuration;
        }
        return readableDuration;
    }

    private static String formatDuration(Duration duration) {

        long hours = Math.abs(duration.toHours());
        long minutes = Math.abs(duration.minus(duration.toHours(), ChronoUnit.HOURS).toMinutes());
        long seconds = Math.abs(duration.minus(duration.toMinutes(), ChronoUnit.MINUTES).getSeconds());

        final String f = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return f;
    }
}
