package de.kopis.timeclicker.utils;

import de.kopis.timeclicker.model.MonthlyTimeSum;
import de.kopis.timeclicker.model.TimeEntry;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MonthlyTimeSumAccumulator {
  private final Map<Instant, MonthlyTimeSum> accumulated = new HashMap<>();

  public void accumulate(final TimeEntry entry1, Duration workPerDay) {
    final Instant month1 = MonthlyTimeSum.makeFirstOfMonth(entry1.getStart());
    if (!accumulated.containsKey(month1)) {
      accumulated.put(month1, new MonthlyTimeSum(entry1.getStart(), Duration.of(0, ChronoUnit.SECONDS), workPerDay));
    }
    accumulated.get(month1).add(entry1);
  }

  public MonthlyTimeSum get(Instant date) {
    return accumulated.get(MonthlyTimeSum.makeFirstOfMonth(date));
  }

  public Collection<MonthlyTimeSum> getAll() {
    return accumulated.values();
  }
}
