package de.kopis.timeclicker.panels;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ActiveEntryPanel extends Panel {

    private static final Logger LOGGER = Logger.getLogger(ActiveEntryPanel.class.getName());
    private final Label label;

    public ActiveEntryPanel(String id) {
        super(id);

        label = new Label("activeEntry", new LoadableDetachableModel<String>() {
            private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

            @Override
            protected String load() {
                String activeEntry = null;

                final UserService userService = UserServiceFactory.getUserService();
                final User user = userService.getCurrentUser();
                // TODO inject TimeclickerAPI
                final TimeclickerAPI api = new TimeclickerAPI();
                try {
                    final TimeEntry latest = api.latest(user);
                    if (latest != null) {
                        final Date start = latest.getStart();
                        activeEntry = DATE_FORMAT.format(start);
                    } else {
                        activeEntry = null;
                    }
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load active entry: " + e.getMessage());
                }

                return activeEntry;
            }
        });
        add(label);
    }

    @Override
    public boolean isVisible() {
        return label.getDefaultModelObject() != null;
    }

}
