package de.kopis.timeclicker.panels;

import org.apache.wicket.markup.html.panel.Panel;

public class FooterPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public FooterPanel(String id) {
        super(id);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        setVersioned(false);
    }

}
