package de.kopis.timeclicker;

import de.kopis.timeclicker.pages.HomePage;
import de.kopis.timeclicker.pages.ListEntriesPage;
import de.kopis.timeclicker.pages.TimeEntryPage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import java.util.logging.Logger;

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

        // configure nice URLs
        mountPage("/home", HomePage.class);
        mountPage("/list", ListEntriesPage.class);
        mountPage("/edit/${key}", TimeEntryPage.class);

        mountResource("/chart.json", new ResourceReference("jsonProducer") {
            @Override
            public IResource getResource() {
                return new ListEntriesChartProducerResource();
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
