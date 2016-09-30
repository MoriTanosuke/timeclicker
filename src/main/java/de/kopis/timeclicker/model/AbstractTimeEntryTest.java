package de.kopis.timeclicker.model;

import java.util.Date;

public class AbstractTimeEntryTest {
    protected static TimeEntry buildTimeEntry(final Date d21, final Date d22) {
        final TimeEntry entryTwoHours = new TimeEntry();
        entryTwoHours.setStart(d21);
        entryTwoHours.setStop(d22);
        return entryTwoHours;
    }
}
