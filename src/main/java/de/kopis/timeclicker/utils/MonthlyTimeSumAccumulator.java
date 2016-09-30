package de.kopis.timeclicker.utils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.kopis.timeclicker.model.MonthlyTimeSum;
import de.kopis.timeclicker.model.TimeEntry;

public class MonthlyTimeSumAccumulator {
    private final Map<Date, MonthlyTimeSum> accumulated = new HashMap<>();

    public void accumulate(final TimeEntry entry1) {
        final Date month1 = MonthlyTimeSum.makeFirstOfMonth(entry1.getStart());
        if (!accumulated.containsKey(month1)) {
            accumulated.put(month1, new MonthlyTimeSum(entry1.getStart(), 0L));
        }
        accumulated.get(month1).add(entry1);
    }

    public MonthlyTimeSum get(Date date) {
        return accumulated.get(MonthlyTimeSum.makeFirstOfMonth(date));
    }

    public Collection<MonthlyTimeSum> getAll() {
        return accumulated.values();
    }
}
