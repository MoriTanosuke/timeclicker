package de.kopis.timeclicker.pages;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.panels.CustomFeedbackPanel;
import de.kopis.timeclicker.panels.FooterPanel;
import de.kopis.timeclicker.panels.HeaderPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.logging.Logger;

public abstract class TemplatePage extends WebPage {
    private static final long serialVersionUID = 1L;
    protected final transient Logger LOGGER;

    protected final transient TimeclickerAPI api;

    private final transient UserService userService = UserServiceFactory.getUserService();

    public TemplatePage(final String header, final PageParameters parameters) {
        super(parameters);
        LOGGER = Logger.getLogger(getClass().getName());
        api = new TimeclickerAPI();

        add(new CustomFeedbackPanel("feedbackPanel"));
        // add all the wicket components
        add(new HeaderPanel("headerPanel"));
        add(new Label("contentHeader", header));
        add(new FooterPanel("footerPanel"));
    }

    public String getLoginURL(String redirectUrl) {
        return userService.createLoginURL(redirectUrl);
    }

    public String getLogoutURL(String redirectUrl) {
        return userService.createLogoutURL(redirectUrl);
    }

    public User getCurrentUser() {
        final User user = userService.getCurrentUser();
        return user;
    }
}
