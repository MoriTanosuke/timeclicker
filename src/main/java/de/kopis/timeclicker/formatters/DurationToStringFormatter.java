package de.kopis.timeclicker.formatters;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.util.Locale;

@Component
public class DurationToStringFormatter implements Formatter<Duration> {
    @Override
    public Duration parse(String text, Locale locale) throws ParseException {
        String[] parts = text.split(":");
        return Duration.ZERO
                .plus(Duration.ofHours(Long.valueOf(parts[0])))
                .plus(Duration.ofMinutes(Long.valueOf(parts[1])))
                .plus(Duration.ofSeconds(Long.valueOf(parts[2])));
    }

    @Override
    public String print(Duration object, Locale locale) {
        return formatDuration(object);
    }

    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%02d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }
}
