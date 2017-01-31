package de.kopis.timeclicker.pages;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
import org.apache.wicket.model.Model;
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

    private Link stop;
    private Link start;
    private Link editLink;

    public HomePage(final PageParameters parameters) {
        super("Home", parameters);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        //TODO check if someone is logged in, else display signin.hint
        final IModel<TimeEntry> activeEntryModel = new LoadableDetachableModel<TimeEntry>() {
            @Override
            protected TimeEntry load() {
                final User user = getCurrentUser();
                TimeEntry latest = null;
                try {
                    latest = getApi().latest(user);
                } catch (NotAuthenticatedException e) {
                    getLOGGER().severe("Can not load active entry: " + e.getMessage());
                }
                return latest;
            }
        };
        final IModel<String> activeSince = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                String activeEntry = null;
                try {
                    final User user = getCurrentUser();
                    final TimeEntry latest = activeEntryModel.getObject();
                    if (latest != null) {
                        Date start = latest.getStart();
                        final Calendar cal = Calendar.getInstance();
                        cal.setTime(start);
                        final TimeZone timezone = getTimeZone(user);
                        final Locale locale = getLocale(user);
                        getLOGGER().fine("Using timezone " + timezone);
                        final DateFormat localeDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
                        localeDateFormat.setTimeZone(timezone);
                        activeEntry = localeDateFormat.format(cal.getTime());
                    }
                } catch (NotAuthenticatedException e) {
                    getLOGGER().severe("Can not load active since date: " + e.getMessage());
                }
                return activeEntry;
            }
        };

        final ActiveEntryPanel activeEntry = new ActiveEntryPanel("activePanel", activeEntryModel, activeSince);
        add(activeEntry);

        add(start = new Link("start") {
            @Override
            protected void onConfigure() {
                setVisible(getCurrentUser() != null && !activeEntry.isVisible());
            }

            @Override
            public void onClick() {
                final User user = getCurrentUser();
                // start a new entry
                if (user == null) {
                    error("You are not logged in.");
                } else {
                    try {
                        final TimeEntry entry = getApi().start(user);
                        success(getString("entry.started", Model.of(entry)));
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
            protected void onConfigure() {
                setVisible(getCurrentUser() != null && activeEntry.isVisible());
            }

            @Override
            public void onClick() {
                final User user = getCurrentUser();
                // stop latest entry
                if (user == null) {
                    error("You are not logged in.");
                } else {
                    try {
                        getApi().stopLatest(user);
                        success(getString("latest.stopped"));
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
        final Label perDayLabel = new Label("dailySum", new StringResourceModel("daily.sum", dailySum)) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getCurrentUser() != null);
            }
        };
        perDayLabel.add(new AjaxSelfUpdatingTimerBehavior(UPDATE_INTERVAL));
        add(perDayLabel);

        add(new Label("loginHint", "To use this application, please sign in.") {
            @Override
            protected void onConfigure() {
                setVisible(getCurrentUser() == null);
            }
        });
    }
}
