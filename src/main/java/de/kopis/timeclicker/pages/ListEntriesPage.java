package de.kopis.timeclicker.pages;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ListEntriesPage extends TemplatePage {
    private static final long serialVersionUID = 1L;

    public ListEntriesPage(PageParameters parameters) {
        super("Time Entries", parameters);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        setStatelessHint(true);
        setVersioned(false);

        final List<TimeEntry> entries = new ArrayList<>();

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        if (user != null) {
            try {
                entries.addAll(api.list(user));
            } catch (NotAuthenticatedException e) {
                LOGGER.severe("Can not load entries for user " + user + ": " + e.getMessage());
            }
        }

        final DateFormat DF = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        add(new ListView<TimeEntry>("listView", entries) {
            @Override
            protected void populateItem(final ListItem<TimeEntry> item) {
                item.add(new Label("entryKey", item.getModelObject().getKey()));
                item.add(new Label("entryStart", DF.format(item.getModelObject().getStart())));
                if (item.getModelObject().getStop() != null) {
                    item.add(new Label("entryStop", DF.format(item.getModelObject().getStop())));
                } else {
                    item.add(new Label("entryStop", "-"));
                }
                item.add(new Link("editLink") {
                    @Override
                    public void onClick() {
                        //TODO implement edit page
                        LOGGER.warning("EDIT not yet implemented");
                        setResponsePage(findPage());
                    }
                });
                item.add(new Link("deleteLink") {
                    @Override
                    public void onClick() {
                        try {
                            api.delete(item.getModelObject().getKey(), user);
                        } catch (NotAuthenticatedException e) {
                            LOGGER.severe("Can not delete entry " + item.getModelObject().getKey());
                        }
                        setResponsePage(findPage());
                    }
                });
            }
        });
    }
}
