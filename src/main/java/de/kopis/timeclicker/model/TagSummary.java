package de.kopis.timeclicker.model;

import de.kopis.timeclicker.utils.DurationUtils;

import java.time.Duration;

public class TagSummary {
  private final String tag;
  private Duration duration;

  public TagSummary(String tag, Duration duration) {
    this.tag = tag;
    this.duration = duration;
  }

  public TagSummary(String tag) {
    this(tag, Duration.ZERO);
  }

  public Duration getDuration() {
    return this.duration;
  }

  public String getReadableDuration() {
    return DurationUtils.getReadableDuration(duration);
  }

  public String getTag() {
    return this.tag;
  }

  public void add(TimeEntry e) {
    // no tags at all
    if (e.getTags() == null || e.getTags().isEmpty()) {
      return;
    }
    // tags available, but no match
    if (!e.getTags().contains(tag)) {
      throw new IllegalArgumentException("Entity not tagged with '" + tag + "'. Tags: " + e.getTags());
    }

    this.duration = this.duration.plus(e.getDuration());
  }
}
