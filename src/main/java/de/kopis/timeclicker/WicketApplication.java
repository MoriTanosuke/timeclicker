package de.kopis.timeclicker;

import de.kopis.timeclicker.pages.HomePage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

import java.util.logging.Logger;

public class WicketApplication extends WebApplication {
    private static final Logger LOGGER = Logger.getLogger(WicketApplication.class.getName());

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        LOGGER.info("Returning application.");
        return HomePage.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();
        LOGGER.info("Initializing application.");

        // add your configuration here
    }
}
