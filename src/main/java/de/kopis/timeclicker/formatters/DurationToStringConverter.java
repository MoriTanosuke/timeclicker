package de.kopis.timeclicker.formatters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DurationToStringConverter implements Converter<Duration, String> {
  @Override
  public String convert(Duration source) {
    return Long.toString(source.toMinutes());
  }
}
