package de.kopis.timeclicker.panels;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;

public class HeaderPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public HeaderPanel(String id) {
        super(id);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        setVersioned(false);

        final UserService userService = UserServiceFactory.getUserService();

        final ExternalLink signin = new ExternalLink("signinButton", userService.createLoginURL("/")) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(UserServiceFactory.getUserService().getCurrentUser() == null);
            }
        };
        add(signin.add(new Label("signinLabel", "Sign in")));

        final ExternalLink signout = new ExternalLink("signoutButton", userService.createLogoutURL("/")) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(UserServiceFactory.getUserService().getCurrentUser() != null);
            }
        };
        add(signout.add(new Label("signoutLabel", "Sign out")));
    }

}
