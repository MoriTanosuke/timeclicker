package de.kopis.timeclicker.formatters;

import de.kopis.timeclicker.Application;

import java.sql.Date;
import java.time.Instant;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class InstantToStringConverter implements Converter<Instant, String> {
  @Override
  public String convert(Instant source) {
    return Application.getDateFormat().format(Date.from(source));
  }
}
