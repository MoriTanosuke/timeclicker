package de.kopis.timeclicker.pages;

import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import de.kopis.timeclicker.panels.ActiveEntryPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class HomePage extends TemplatePage {
    private static final long serialVersionUID = 1L;

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

        add(start = new Link("start") {
            @Override
            public void onClick() {
                // start a new entry
                if (getCurrentUser() == null) {
                    //TODO add error or redirect to login
                    error("You are not logged in.");
                } else {
                    try {
                        final TimeEntry entry = api.start(getCurrentUser());
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
                        api.stopLatest(getCurrentUser());
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

        try {
            if (getCurrentUser() != null) {
                //TODO implement LoadableDetachableModel with sums
                add(new Label("dailySum", "Daily: " + getReadableDuration(api.getDailySum(getCurrentUser()))));
                add(new Label("weeklySum", "Weekly: " + getReadableDuration(api.getWeeklySum(getCurrentUser()))));
                add(new Label("monthlySum", "Monthly: " + getReadableDuration(api.getMonthlySum(getCurrentUser()))));
                add(new Label("overallSum", "Overall: " + getReadableDuration(api.getOverallSum(getCurrentUser()))));
            } else {
                add(new Label("dailySum", "Daily: 0"));
                add(new Label("weeklySum", "Weekly: 0"));
                add(new Label("monthlySum", "Monthly: 0"));
                add(new Label("overallSum", "Overall: 0"));
            }
        } catch (NotAuthenticatedException e) {
            throw new RedirectToUrlException(getLoginURL("/"));
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
