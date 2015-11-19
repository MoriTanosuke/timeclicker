package de.kopis.timeclicker.pages;

import com.google.appengine.api.users.User;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import de.kopis.timeclicker.panels.ActiveEntryPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.text.SimpleDateFormat;
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
        setStatelessHint(true);
        setVersioned(false);

        add(activeEntry = new ActiveEntryPanel("activePanel", new LoadableDetachableModel<String>() {
            private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

            @Override
            protected String load() {
                String activeEntry = null;

                final User user = getCurrentUser();
                if (user == null) return null;

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
            public void onClick() {
                // start a new entry
                if (getCurrentUser() == null) {
                    //TODO add error or redirect to login
                    error("You are not logged in.");
                } else {
                    try {
                        final TimeEntry entry = getApi().start(getCurrentUser());
                        // TODO make the links figure visibility out themselves
                        start.setVisible(false);
                        stop.setVisible(true);
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
            public void onClick() {
                // stop latest entry
                if (getCurrentUser() == null) {
                    //TODO add error or redirect to login
                    error("You are not logged in.");
                } else {
                    try {
                        getApi().stopLatest(getCurrentUser());
                        //TODO how to invalidate activeEntry model?
                        activeEntry.modelChanged();
                        // TODO make the links figure visibility out themselves
                        start.setVisible(true);
                        stop.setVisible(false);
                        success("Latest entry stopped.");
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not stop entry: " + e.getMessage());
                        error(e.getMessage());
                    }
                }
                setResponsePage(findPage());
            }
        });

        // TODO make the links figure visibility out themselves
        start.setVisible(!activeEntry.isVisible());
        stop.setVisible(activeEntry.isVisible());

        if (getCurrentUser() != null) {
            //TODO implement LoadableDetachableModel with sums
            add(new Label("dailySum", new LoadableDetachableModel<String>() {
                @Override
                protected String load() {
                    String s = null;
                    try {
                        s = "Daily: " +
                                getReadableDuration(HomePage.this.getApi().getDailySum(getCurrentUser()));
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not load daily time sum: " + e.getMessage());
                    }
                    return s;
                }
            }));
            add(new Label("weeklySum", new LoadableDetachableModel<String>() {
                @Override
                protected String load() {
                    String s = null;
                    try {
                        s = "Weekly: " + getReadableDuration(getApi().getWeeklySum(getCurrentUser()));
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not load daily time sum: " + e.getMessage());
                    }
                    return s;
                }
            }));
            add(new Label("monthlySum", new LoadableDetachableModel<String>() {
                @Override
                protected String load() {
                    String s = null;
                    try {
                        s = "Monthly: " + getReadableDuration(getApi().getMonthlySum(getCurrentUser()));
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not load daily time sum: " + e.getMessage());
                    }
                    return s;
                }
            }));
            add(new Label("overallSum", new LoadableDetachableModel<String>() {
                @Override
                protected String load() {
                    String s = null;
                    try {
                        s = "Overall: " + getReadableDuration(getApi().getOverallSum(getCurrentUser()));
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not load daily time sum: " + e.getMessage());
                    }
                    return s;
                }
            }));
        } else {
            add(new Label("dailySum", Model.of("Daily: 0")));
            add(new Label("weeklySum", Model.of("Weekly: 0")));
            add(new Label("monthlySum", Model.of("Monthly: 0")));
            add(new Label("overallSum", Model.of("Overall: 0")));
        }
    }

    private String getReadableDuration(TimeSum sum) throws NotAuthenticatedException {
        final long duration = sum.getDuration();
        //TODO use formatter with model

        String readableDuration = "" + duration;
        try {
            Duration d = DatatypeFactory.newInstance().newDuration(duration);
            readableDuration = String.format("%02d hours, %02d minutes, %02d seconds", d.getDays() * 24 + d.getHours(), d.getMinutes(), d.getSeconds());
        } catch (DatatypeConfigurationException e) {
            LOGGER.severe("Can not format duration: " + e.getMessage());
        }
        return readableDuration;
    }
}
