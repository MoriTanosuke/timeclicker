package de.kopis.timeclicker;

import de.kopis.timeclicker.pages.HomePage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

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
        //mountPage("/", HomePage.class);
        //mountPage("/list", ListEntriesPage.class);
    }
}
