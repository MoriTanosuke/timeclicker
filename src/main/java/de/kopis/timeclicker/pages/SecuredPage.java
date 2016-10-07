package de.kopis.timeclicker.pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class SecuredPage extends TemplatePage {

    public SecuredPage(String header, PageParameters parameters) {
        super(header, parameters);
    }

    public boolean isAuthorized() {
        return getCurrentUser() != null;
    }
}
