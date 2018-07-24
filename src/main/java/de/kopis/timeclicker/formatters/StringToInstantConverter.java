package de.kopis.timeclicker.formatters;

import de.kopis.timeclicker.Application;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;

@Component
public class StringToInstantConverter implements Converter<String, Instant> {
  @Override
  public Instant convert(String source) {
    if (source == null || source.isEmpty()) return null;

    try {
      return Application.getDateFormat().parse(source).toInstant();
    } catch (ParseException e) {
      throw new IllegalArgumentException("Can not parse date: " + source, e);
    }
  }
}
