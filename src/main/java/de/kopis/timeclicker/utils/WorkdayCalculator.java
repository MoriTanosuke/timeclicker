package de.kopis.timeclicker.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class WorkdayCalculator {
    private static final Logger LOGGER = Logger.getLogger(WorkdayCalculator.class.getName());

    public static int getWorkingDays(final Date startDate, final Date endDate) {
        LOGGER.fine("Calculating work days from " + startDate + " to " + endDate);

        final Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        final Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        int workDays = 0;

        //Return 1 if start and end are the same
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return 1;
        }

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTime(endDate);
            endCal.setTime(startDate);
        }

        while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()) {
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                ++workDays;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // dirty workaround for div by zero
        if (workDays == 0) {
            workDays = 1;
        }

        LOGGER.fine("Workdays: " + workDays);
        return workDays;
    }
}
