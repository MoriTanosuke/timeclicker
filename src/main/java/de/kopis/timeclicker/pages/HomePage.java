package de.kopis.timeclicker.pages;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class HomePage extends TemplatePage {
    private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
        super("Statistics", parameters);

    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        setStatelessHint(true);
        setVersioned(false);

        add(new Link("start") {
            @Override
            public void onClick() {
                // start a new entry
                UserService userService = UserServiceFactory.getUserService();
                User user = userService.getCurrentUser();
                if (user == null) {
                    //TODO add error or redirect to login
                    LOGGER.info("Not logged in, have to redirect...");
                } else {
                    try {
                        api.start(user);
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not start entry: " + e.getMessage());
                    }
                    //TODO set flash message "Entry started"
                }
                setResponsePage(findPage());
            }
        });
        add(new Link("stop") {
            @Override
            public void onClick() {
                // stop latest entry
                UserService userService = UserServiceFactory.getUserService();
                User user = userService.getCurrentUser();
                if (user == null) {
                    //TODO add error or redirect to login
                    LOGGER.info("Not logged in, have to redirect...");
                } else {
                    try {
                        api.stopLatest(user);
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not stop entry: " + e.getMessage());
                    }
                    //TODO set flash message "Entry started"
                }
                setResponsePage(findPage());
            }
        });

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        try {
            Label currentEntryLabel;
            if (user != null) {
                add(new Label("dailySum", "Daily: " + getReadableDuration(api.getDailySum(user))));
                add(new Label("weeklySum", "Weekly: " + getReadableDuration(api.getWeeklySum(user))));
                add(new Label("monthlySum", "Monthly: " + getReadableDuration(api.getMonthlySum(user))));
                add(new Label("overallSum", "Overall: " + getReadableDuration(api.getOverallSum(user))));

                TimeEntry latest = api.latest(user);
                if (latest != null) {
                    currentEntryLabel = new Label("currentEntry", "Tracking since: " + latest.getStart());
                    currentEntryLabel.add(new AttributeModifier("class", "alert alert-info"));
                } else {
                    currentEntryLabel = new Label("currentEntry", "");
                    currentEntryLabel.add(new AttributeModifier("style", "display: none;"));
                }
            } else {
                // TODO remove duplication of label "currentEntry"
                currentEntryLabel = new Label("currentEntry", "");
                currentEntryLabel.add(new AttributeModifier("style", "display: none;"));

                add(new Label("dailySum", "Daily: 0"));
                add(new Label("weeklySum", "Weekly: 0"));
                add(new Label("monthlySum", "Monthly: 0"));
                add(new Label("overallSum", "Overall: 0"));
            }
            add(currentEntryLabel);
        } catch (NotAuthenticatedException e) {
            throw new RedirectToUrlException(userService.createLoginURL("/"));
        }
    }

    private String getReadableDuration(TimeSum sum) throws NotAuthenticatedException {
        final long duration = sum.getDuration();

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
