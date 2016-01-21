package de.kopis.timeclicker.pages;

import com.google.appengine.api.users.User;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import de.kopis.timeclicker.panels.ActiveEntryPanel;
import de.kopis.timeclicker.utils.WorkdayCalculator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HomePage extends TemplatePage {
    private static final long serialVersionUID = 1L;

    private ActiveEntryPanel activeEntry;
    private Link stop;
    private Link start;

    public HomePage(final PageParameters parameters) {
        super("Statistics", parameters);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        add(activeEntry = new ActiveEntryPanel("activePanel", new LoadableDetachableModel<String>() {
            private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

            @Override
            protected String load() {
                String activeEntry = null;

                final User user = getCurrentUser();
                if (user == null) {
                    return null;
                }

                try {
                    final TimeEntry latest = getApi().latest(user);
                    if (latest != null) {
                        final Date start = latest.getStart();
                        activeEntry = DATE_FORMAT.format(start);
                    } else {
                        activeEntry = null;
                    }
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load active entry: " + e.getMessage());
                }

                return activeEntry;
            }
        }));
        add(start = new Link("start") {
            @Override
            public boolean isVisible() {
                return !activeEntry.isVisible();
            }

            @Override
            public void onClick() {
                // start a new entry
                if (getCurrentUser() == null) {
                    // TODO add error or redirect to login
                    error("You are not logged in.");
                } else {
                    try {
                        final TimeEntry entry = getApi().start(getCurrentUser());
                        success("Entry " + entry.getKey() + " started.");
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not start entry: " + e.getMessage());
                        error(e.getMessage());
                    }
                }
                setResponsePage(findPage());
            }
        });
        add(stop = new Link("stop") {
            @Override
            public boolean isVisible() {
                return activeEntry.isVisible();
            }

            @Override
            public void onClick() {
                // stop latest entry
                if (getCurrentUser() == null) {
                    // TODO add error or redirect to login
                    error("You are not logged in.");
                } else {
                    try {
                        getApi().stopLatest(getCurrentUser());
                        // TODO how to invalidate activeEntry model?
                        activeEntry.modelChanged();
                        success("Latest entry stopped.");
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not stop entry: " + e.getMessage());
                        error(e.getMessage());
                    }
                }
                setResponsePage(findPage());
            }
        });

        final IModel<TimeSum> overallSum = new LoadableDetachableModel<TimeSum>() {
            private TimeSum sum;

            @Override
            protected TimeSum load() {
                try {
                    sum = getApi().getOverallSum(getCurrentUser());
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load overall sum: " + e.getMessage());
                }
                return sum;
            }
        };
        final IModel<TimeSum> monthlySum = new LoadableDetachableModel<TimeSum>() {
            private TimeSum sum;

            @Override
            protected TimeSum load() {
                try {
                    sum = getApi().getMonthlySum(getCurrentUser());
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load monthly sum: " + e.getMessage());
                }
                return sum;
            }
        };
        final IModel<TimeSum> weeklySum = new LoadableDetachableModel<TimeSum>() {
            private TimeSum sum;

            @Override
            protected TimeSum load() {
                try {
                    sum = getApi().getWeeklySum(getCurrentUser());
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load weekly sum: " + e.getMessage());
                }
                return sum;
            }
        };
        final IModel<TimeSum> dailySum = new LoadableDetachableModel<TimeSum>() {
            private TimeSum sum;

            @Override
            protected TimeSum load() {
                try {
                    sum = getApi().getDailySum(getCurrentUser());
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load daily sum: " + e.getMessage());
                }
                return sum;
            }
        };

        final IModel<Integer> workingDaysInMonth = new LoadableDetachableModel<Integer>() {
            @Override
            protected Integer load() {
                final Calendar cal = Calendar.getInstance();
                // end today midnight
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                final Date endDate = cal.getTime();
                // start first of month
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                final Date startDate = cal.getTime();
                return WorkdayCalculator.getWorkingDays(startDate, endDate);
            }
        };
        final IModel<TimeSum> averagePerWorkday = new LoadableDetachableModel<TimeSum>() {
            @Override
            protected TimeSum load() {
                final double workdays = workingDaysInMonth.getObject();
                double averagePerDay = 0;

                final TimeSum monthlyTimeSum = monthlySum.getObject();
                if (monthlyTimeSum != null) {
                    averagePerDay = monthlyTimeSum.getDuration() / workdays;
                }
                return new TimeSum((long) averagePerDay);
            }
        };

        //TODO display averagePerWorkday as readable duration
        add(new Label("averagePerDay", new StringResourceModel("average.sum", null, averagePerWorkday.getObject())));
        //TODO sums are not updating on page refresh!
        add(new Label("dailySum", new StringResourceModel("daily.sum", null, dailySum.getObject())));
        add(new Label("weeklySum", new StringResourceModel("weekly.sum", null, weeklySum.getObject())));
        add(new Label("monthlySum", new StringResourceModel("monthly.sum", null, monthlySum.getObject())));
        add(new Label("sum", new StringResourceModel("overall.sum", null, overallSum.getObject())));
    }
}
