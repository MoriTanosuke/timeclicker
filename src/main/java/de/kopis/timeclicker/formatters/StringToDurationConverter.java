package de.kopis.timeclicker.formatters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
public class StringToDurationConverter implements Converter<String, Duration> {
    @Override
    public Duration convert(String source) {
        return Duration.of(Long.valueOf(source), ChronoUnit.MILLIS);
    }
}
