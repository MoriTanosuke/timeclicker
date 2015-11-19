package de.kopis.timeclicker.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.util.logging.Logger;

public class ActiveEntryPanel extends Panel {

    private static final Logger LOGGER = Logger.getLogger(ActiveEntryPanel.class.getName());
    private final Label label;

    public ActiveEntryPanel(String id, IModel model) {
        super(id);

        label = new Label("activeEntry", model);
        add(label);
    }

    @Override
    public boolean isVisible() {
        return label.getDefaultModelObject() != null;
    }

}
