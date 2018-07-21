package de.kopis.timeclicker;

import org.springframework.format.Formatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class DateFormatter implements Formatter<Date> {
    @Override
    public Date parse(String source, Locale locale) throws ParseException {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        try {
            return dateFormat.parse(source);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can not convert into date: " + source, e);
        }
    }

    @Override
    public String print(Date source, Locale locale) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        return dateFormat.format(source);
    }
}
