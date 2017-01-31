package de.kopis.timeclicker.panels;

import java.util.logging.Logger;

import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.pages.TimeEntryPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ActiveEntryPanel extends Panel {

    private static final Logger LOGGER = Logger.getLogger(ActiveEntryPanel.class.getName());
    private final Label label;

    public ActiveEntryPanel(final String id, final IModel<TimeEntry> activeEntryModel, final IModel<String> sinceModel) {
        super(id);

        label = new Label("activeEntry", sinceModel);
        add(label);

        add(new Link("edit") {
            @Override
            public void onClick() {
                PageParameters params = new PageParameters();
                params.add("key", activeEntryModel.getObject().getKey());
                final TimeEntryPage page = new TimeEntryPage(params);
                page.setBackPage(getPage());
                setResponsePage(page);
            }
        });
    }

    @Override
    public void onConfigure() {
        setVisible(label.getDefaultModelObject() != null);
    }

}
