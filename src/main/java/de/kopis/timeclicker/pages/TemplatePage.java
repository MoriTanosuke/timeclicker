package de.kopis.timeclicker.pages;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.panels.FooterPanel;
import de.kopis.timeclicker.panels.HeaderPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.logging.Logger;

public abstract class TemplatePage extends WebPage {
    private static final long serialVersionUID = 1L;
    protected final transient Logger LOGGER;

    protected static final TimeclickerAPI api = new TimeclickerAPI();

    public TemplatePage(final String header, final PageParameters parameters) {
        super(parameters);
        LOGGER = Logger.getLogger(getClass().getName());

        add(new Label("contentHeader", header));
        // add all the wicket components
        add(new HeaderPanel("headerPanel"));
        add(new FooterPanel("footerPanel"));
    }
}
