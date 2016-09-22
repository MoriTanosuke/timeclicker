package de.kopis.timeclicker.pages;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import de.kopis.timeclicker.panels.ActiveEntryPanel;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;

import com.google.appengine.api.users.User;

public class HomePage extends TemplatePage {
    private static final long serialVersionUID = 1L;
    /**
     * Update interval for sums.
     */
    public static final Duration UPDATE_INTERVAL = Duration.hours(2);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private ActiveEntryPanel activeEntry;
    private Link stop;
    private Link start;

    public HomePage(final PageParameters parameters) {
        super("Home", parameters);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        add(activeEntry = new ActiveEntryPanel("activePanel", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                String activeEntry = null;

                final User user = getCurrentUser();
                if (user == null) {
                    return null;
                }

                try {
                    final TimeEntry latest = getApi().latest(user);
                    final TimeZone timezone = getTimeZone(user);
                    if (latest != null) {
                        final Date start = latest.getStart();

                        final Calendar cal = Calendar.getInstance();
                        cal.setTime(start);
                        getLOGGER().fine("Using timezone " + timezone);
                        DATE_FORMAT.setTimeZone(timezone);
                        activeEntry = DATE_FORMAT.format(cal.getTime());
                    } else {
                        activeEntry = null;
                    }
                } catch (NotAuthenticatedException e) {
                    getLOGGER().severe("Can not load active entry: " + e.getMessage());
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
                    error("You are not logged in.");
                } else {
                    try {
                        final TimeEntry entry = getApi().start(getCurrentUser());
                        success("Entry " + entry.getKey() + " started.");
                    } catch (NotAuthenticatedException e) {
                        getLOGGER().severe("Can not start entry: " + e.getMessage());
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
                    error("You are not logged in.");
                } else {
                    try {
                        getApi().stopLatest(getCurrentUser());
                        // TODO how to invalidate activeEntry model?
                        activeEntry.modelChanged();
                        success("Latest entry stopped.");
                    } catch (NotAuthenticatedException e) {
                        getLOGGER().severe("Can not stop entry: " + e.getMessage());
                        error(e.getMessage());
                    }
                }
                setResponsePage(findPage());
            }
        });

        final IModel<TimeSum> dailySum = new LoadableDetachableModel<TimeSum>() {
            private TimeSum sum;

            @Override
            protected TimeSum load() {
                try {
                    sum = getApi().getDailySum(getCurrentUser());
                } catch (NotAuthenticatedException e) {
                    getLOGGER().severe("Can not load daily sum: " + e.getMessage());
                }
                return sum;
            }
        };
        final Label perDayLabel = new Label("dailySum", new StringResourceModel("daily.sum", dailySum));
        perDayLabel.add(new AjaxSelfUpdatingTimerBehavior(UPDATE_INTERVAL));
        add(perDayLabel);
    }
}
