package de.kopis.timeclicker.formatters;

import java.time.Duration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DurationToStringConverter implements Converter<Duration, String> {
  @Override
  public String convert(Duration source) {
    // return in minutes
    return Long.toString(source.toMinutes());
  }
}
