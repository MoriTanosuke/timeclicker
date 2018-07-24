package de.kopis.timeclicker.utils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormattedDurationPrinter {
  private static final Logger LOGGER = LoggerFactory.getLogger(FormattedDurationPrinter.class);

  public static String getReadableDuration(long duration) {
    // TODO use formatter with model

    String readableDuration = "" + duration;
    try {
      Duration d = DatatypeFactory.newInstance().newDuration(duration);
      readableDuration = String.format("%02d hours, %02d minutes, %02d seconds", d.getDays() * 24 + d.getHours(),
          d.getMinutes(), d.getSeconds());
    } catch (DatatypeConfigurationException e) {
      LOGGER.warn("Can not format duration: " + e.getMessage());
    }
    return readableDuration;
  }
}
