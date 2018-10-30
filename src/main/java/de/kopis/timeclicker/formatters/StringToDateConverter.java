package de.kopis.timeclicker.formatters;

import de.kopis.timeclicker.Application;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class StringToDateConverter implements Converter<String, Date> {
  @Override
  public Date convert(String source) {
    if (source == null || source.isEmpty()) return null;

    try {
      return Application.getDateFormat().parse(source);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Can not parse date using format '" + Application.getDateFormat().toPattern() + "': " + source, e);
    }
  }
}
