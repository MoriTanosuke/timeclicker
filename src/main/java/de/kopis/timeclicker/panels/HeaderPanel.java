package de.kopis.timeclicker.panels;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;

public class HeaderPanel extends Panel {
    public HeaderPanel(String id) {
        super(id);

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        final String url = getUrl(userService, user);
        final String label = getLabel(user);

        add(new ExternalLink("signinButton", url).add(new Label("signinLabel", label)));
    }

    private String getLabel(User user) {
        String label = "Sign in";
        if (user != null) {
            label = "Sign out";
        }
        return label;
    }

    private String getUrl(UserService userService, User user) {
        String url;
        if (user == null) {
            url = userService.createLoginURL("/");
        } else {
            url = userService.createLogoutURL("/");
        }
        return url;
    }
}
