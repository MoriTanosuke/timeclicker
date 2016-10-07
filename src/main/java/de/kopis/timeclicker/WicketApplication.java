package de.kopis.timeclicker;

import java.util.logging.Logger;

import com.google.appengine.api.users.UserServiceFactory;
import de.agilecoders.wicket.webjars.WicketWebjars;
import de.agilecoders.wicket.webjars.settings.ResourceStreamProvider;
import de.agilecoders.wicket.webjars.settings.WebjarsSettings;
import de.kopis.timeclicker.pages.*;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class WicketApplication extends WebApplication {
    public static final int HOURS_PER_DAY = 8;
    public static final int HOURS_PER_DAY_IN_MILLISECONDS = HOURS_PER_DAY * 60 * 60 * 1000;

    private static final Logger LOGGER = Logger.getLogger(WicketApplication.class.getName());

    @Override
    public Class<? extends WebPage> getHomePage() {
        LOGGER.finer("Returning application.");
        return HomePage.class;
    }

    @Override
    public void init() {
        super.init();
        LOGGER.info("Initializing application.");

        // initialize webjar resources with ClassLoader provider
        WicketWebjars.install(this, new WebjarsSettings().resourceStreamProvider(ResourceStreamProvider.ClassLoader));

        // configure nice URLs
        mountPage("/home", HomePage.class);
        mountPage("/list", ListEntriesPage.class);
        mountPage("/sum", ListSumPage.class);
        mountPage("/monthly", MonthlyListSumPage.class);
        mountPage("/edit/${key}", TimeEntryPage.class);
        mountPage("/add", TimeEntryPage.class);
        mountPage("/settings", UserSettingsPage.class);

        mountResource("/chart.json", new ResourceReference("jsonProducer") {
            @Override
            public IResource getResource() {
                return new ListEntriesChartProducerResource();
            }
        });
        mountResource("/sumchart.json", new ResourceReference("jsonProducer") {
            @Override
            public IResource getResource() {
                return new SumChartProducerResource();
            }
        });
        mountResource("/list.csv", new ResourceReference("csvProducer") {
            @Override
            public IResource getResource() {
                return new ListEntriesCsvProducerResource();
            }
        });

        // set up auth strategy
        final SimplePageAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
                SecuredPage.class, HomePage.class) {
            protected boolean isAuthorized() {
                return UserServiceFactory.getUserService().isUserLoggedIn();
            }
        };
        getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
    }
}
