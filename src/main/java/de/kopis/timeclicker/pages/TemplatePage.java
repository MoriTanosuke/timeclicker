package de.kopis.timeclicker.pages;

import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.wicket.Page;
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
import com.googlecode.wickedcharts.wicket6.JavaScriptResourceRegistry;
import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.UserSettings;
import de.kopis.timeclicker.panels.CustomFeedbackPanel;
import de.kopis.timeclicker.panels.FooterPanel;
import de.kopis.timeclicker.panels.HeaderPanel;

public abstract class TemplatePage extends WebPage {
    private static final long serialVersionUID = 1L;

    private transient Logger LOGGER;

    private transient TimeclickerAPI api;

    private transient UserService userService;
    private Page backPage;

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

    protected long getDailyDuration(final User user) {
        long dailyDuration = 0L;
        try {
            final UserSettings settings = getApi().getUserSettings(null, user);
            dailyDuration = settings.getWorkingDurationPerDay();
        } catch (NotAuthenticatedException | EntityNotFoundException e) {
            getLOGGER().warning("Can not load settings for user " + user + ".");
        }
        return dailyDuration;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        final WebjarsJavaScriptResourceReference jqueryReference = new WebjarsJavaScriptResourceReference("jquery/1.11.3/jquery.js");
        response.render(JavaScriptHeaderItem.forReference(
                jqueryReference));
        response.render(JavaScriptHeaderItem.forReference(
                new WebjarsJavaScriptResourceReference("bootstrap/3.3.6/js/bootstrap.js")));
        JavaScriptResourceRegistry.getInstance().setHighchartsReference(new WebjarsJavaScriptResourceReference("highcharts/5.0.4/highcharts.js"));
        JavaScriptResourceRegistry.getInstance().setHighchartsMoreReference(new WebjarsJavaScriptResourceReference("highcharts/5.0.4/highcharts-more.js"));
        JavaScriptResourceRegistry.getInstance().setHighchartsExportingReference("//code.highcharts.com/modules/exporting.js");
        JavaScriptResourceRegistry.getInstance().setJQueryReference(jqueryReference);

        response.render(CssHeaderItem.forReference(
                new WebjarsCssResourceReference("bootstrap/3.3.6/css/bootstrap.css")));
        //TODO add html5shiv?
        //TODO add respond?
    }

    public void setBackPage(Page backPage) {
        this.backPage = backPage;
    }

    public Page getBackPage() {
        return backPage;
    }

    protected Logger getLOGGER() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(getClass().getName());
        }
        return LOGGER;
    }
}
