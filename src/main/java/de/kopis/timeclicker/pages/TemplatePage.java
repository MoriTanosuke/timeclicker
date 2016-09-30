package de.kopis.timeclicker.pages;

import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.UserSettings;
import de.kopis.timeclicker.panels.CustomFeedbackPanel;
import de.kopis.timeclicker.panels.FooterPanel;
import de.kopis.timeclicker.panels.HeaderPanel;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public abstract class TemplatePage extends WebPage {
    private static final long serialVersionUID = 1L;

    private transient Logger LOGGER;

    private transient TimeclickerAPI api;

    private transient UserService userService;

    public TemplatePage(final String header, final PageParameters parameters) {
        super(parameters);
        LOGGER = Logger.getLogger(getClass().getName());

        final User user = getCurrentUser();
        if (user != null) {
            try {
                UserSettings settings = new TimeclickerAPI().getUserSettings(null, user);
                LOGGER.info("Setting locale " + settings.getLocale());
                Session.get().setLocale(settings.getLocale());
            } catch (NotAuthenticatedException | EntityNotFoundException e) {
                LOGGER.fine("Can not load user locale, using default " + Session.get().getLocale());
            }
        }

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
        final User user = getUserService().getCurrentUser();
        return user;
    }

    public TimeclickerAPI getApi() {
        if (api == null) {
            api = new TimeclickerAPI();
        }
        return api;
    }

    public UserService getUserService() {
        if (userService == null) {
            userService = UserServiceFactory.getUserService();
        }
        return userService;
    }

    protected TimeZone getTimeZone(User user) throws NotAuthenticatedException {
        final UserSettings settings;
        TimeZone timezone = TimeZone.getDefault();
        try {
            settings = getApi().getUserSettings(null, user);
            timezone = settings.getTimezone();
        } catch (EntityNotFoundException e) {
            getLOGGER().warning("Can not load settings for user " + user + ". Using default timezone " + timezone.getID());
        }
        return timezone;
    }

    protected Locale getLocale(User user) throws NotAuthenticatedException {
        final UserSettings settings;
        Locale locale = Locale.getDefault();
        try {
            settings = getApi().getUserSettings(null, user);
            locale = settings.getLocale();
        } catch (EntityNotFoundException e) {
            getLOGGER().warning("Can not load settings for user " + user + ". Using default locale " + locale.getDisplayName());
        }
        return locale;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(
                new WebjarsJavaScriptResourceReference("jquery/1.11.3/jquery.js")));
        response.render(JavaScriptHeaderItem.forReference(
                new WebjarsJavaScriptResourceReference("bootstrap/3.3.6/js/bootstrap.js")));

        response.render(CssHeaderItem.forReference(
                new WebjarsCssResourceReference("bootstrap/3.3.6/css/bootstrap.css")));
        //TODO add html5shiv?
        //TODO add respond?
    }

    protected Logger getLOGGER() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(getClass().getName());
        }
        return LOGGER;
    }
}
