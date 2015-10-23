package de.kopis.timeclicker.pages;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Locale;

public class TimeEntryPage extends TemplatePage {

    public TimeEntryPage(PageParameters parameters) {
        super("Edit entry", parameters);

        final String key = parameters.get("key").toString();
        // load TimeEntry by key
        if (getCurrentUser() == null) {
            error("Not authenticated!");
        }

        try {
            final TimeEntry entry = api.show(key, getCurrentUser());
            //TODO extract Form?
            final Form<TimeEntry> form = new Form<>("entryForm");
            form.setDefaultModel(new CompoundPropertyModel(entry));
            form.add(new HiddenField("key"));
            form.add(new DateTimeField("start"));
            //TODO add timezone to DateTimeField?
            form.add(new DateTimeField("stop"));
            form.add(new Button("update") {
                @Override
                public void onSubmit() {
                    //TODO implement LoadableDetachableModel with TimeEntry!
                    TimeEntry updateEntry = (TimeEntry) getForm().getModel().getObject();
                    try {
                        UserService userService = UserServiceFactory.getUserService();
                        User user = userService.getCurrentUser();
                        api.update(updateEntry.getKey(), updateEntry.getStart(), updateEntry.getStop(), user);
                        success("Entry saved.");
                    } catch (NotAuthenticatedException e) {
                        error("Can not save entry. Try again.");
                    }
                }
            });
            add(form);
        } catch (NotAuthenticatedException e) {
            error(e.getMessage());
        }
    }
}
