package de.kopis.timeclicker;

import java.util.logging.Logger;

import de.agilecoders.wicket.webjars.WicketWebjars;
import de.agilecoders.wicket.webjars.settings.ResourceStreamProvider;
import de.agilecoders.wicket.webjars.settings.WebjarsSettings;
import de.kopis.timeclicker.pages.HomePage;
import de.kopis.timeclicker.pages.ListEntriesPage;
import de.kopis.timeclicker.pages.ListSumPage;
import de.kopis.timeclicker.pages.MonthlyListSumPage;
import de.kopis.timeclicker.pages.TimeEntryPage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class WicketApplication extends WebApplication {
    private static final Logger LOGGER = Logger.getLogger(WicketApplication.class.getName());

    @Override
    public Class<? extends WebPage> getHomePage() {
        LOGGER.fine("Returning application.");
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
    }
}
