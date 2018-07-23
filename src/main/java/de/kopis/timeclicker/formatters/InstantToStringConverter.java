package de.kopis.timeclicker.formatters;

import de.kopis.timeclicker.Application;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;

@Component
public class InstantToStringConverter implements Converter<Instant, String> {
    @Override
    public String convert(Instant source) {
        return Application.DATE_FORMAT.format(Date.from(source));
    }
}
