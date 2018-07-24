package de.kopis.timeclicker.model;

import de.kopis.timeclicker.utils.DurationUtils;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSum implements Serializable, Comparable {
  private static final Logger LOGGER = LoggerFactory.getLogger(TimeSum.class);

  private static final long serialVersionUID = 1L;
  private Duration duration;

  public TimeSum(Duration duration) {
    this.duration = duration;
  }

  public TimeSum(TimeEntry entry) {
    this.duration = calculateDuration(entry);
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public void addDuration(Duration duration) {
    this.duration = this.duration.plus(duration);
    LOGGER.debug("Added duration {}, sum is now {}", duration, this.duration);
  }

  public void add(final TimeSum sum) {
    Duration duration = sum.getDuration();
    addDuration(duration);
  }

  private Duration calculateDuration(TimeEntry entry) {
    final Instant start = entry.getStart();
    // check if the entity is already stopped, else use the current date
    Instant stop = entry.getStop();
    if (stop == null) {
      stop = Instant.now();
    }

    Duration duration = Duration.between(start, stop).minus(entry.getBreakDuration());
    LOGGER.debug("Calculated duration: " + duration);
    return duration;
  }

  @Override
  public String toString() {
    return getReadableDuration(this);
  }

  public String getReadableDuration() {
    return getReadableDuration(this);
  }

  private String getReadableDuration(TimeSum sum) {
    if (sum != null) {
      return DurationUtils.getReadableDuration(sum.getDuration());
    } else {
      return "--";
    }
  }

  @Override
  public int compareTo(Object o) {
    if (!(o instanceof TimeSum)) return -1;

    TimeSum other = (TimeSum) o;
    return other.duration.compareTo(duration);
  }
}
