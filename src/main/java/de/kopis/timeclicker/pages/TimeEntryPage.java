package de.kopis.timeclicker.pages;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class TimeEntryPage extends TemplatePage {

    public TimeEntryPage(PageParameters parameters) {
        super("Edit entry", parameters);

        if (getCurrentUser() == null) {
            error("Not authenticated!");
        }

        final String key = parameters.get("key").toString();

        try {
            // load TimeEntry by key
            TimeEntry entry;
            if (key != null) {
                entry = getApi().show(key, getCurrentUser());
            } else {
                entry = new TimeEntry();
            }
            //TODO extract Form?
            final Form<TimeEntry> form = new Form<>("entryForm");
            form.setDefaultModel(new CompoundPropertyModel(entry));
            form.add(new HiddenField("key"));
            //TODO add timezone to DateTimeField?
            form.add(new DateTimeField("start"));
            form.add(new DateTimeField("stop"));
            form.add(new TextField("tags"));
            form.add(new Button("update") {
                @Override
                public void onSubmit() {
                    //TODO implement LoadableDetachableModel with TimeEntry!
                    TimeEntry updateEntry = (TimeEntry) getForm().getModel().getObject();
                    try {
                        UserService userService = UserServiceFactory.getUserService();
                        User user = userService.getCurrentUser();
                        getApi().update(updateEntry.getKey(), updateEntry.getStart(), updateEntry.getStop(), updateEntry.getTags(), user);
                        success("Entry saved.");
                    } catch (NotAuthenticatedException e) {
                        error("Can not save entry. Try again.");
                    }
                }
            });
            add(form);
        } catch (NotAuthenticatedException e) {
            error("Can not load entry, you're not authenticated");
        }
    }
}
