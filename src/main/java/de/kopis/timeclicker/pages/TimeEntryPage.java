package de.kopis.timeclicker.pages;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class TimeEntryPage extends TemplatePage {
    public TimeEntryPage(PageParameters parameters) {
        super("Edit entry", parameters);

        add(new FeedbackPanel("feedbackPanel"));

        StringValue key = parameters.get("key");
        // load TimeEntry by key
        UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        if (user == null) {
            error("Not authenticated!");
        }
        try {
            final TimeEntry entry = api.show(key.toString(), user);
            Form entryForm = new Form("thisEntryForm", new CompoundPropertyModel<TimeEntry>(entry)) {
                @Override
                protected void onSubmit() {
                    super.onSubmit();
                    LOGGER.info("Submitting entry: " + getModel());
                    TimeEntry updateEntry = (TimeEntry) getModel().getObject();
                    try {
                        api.update(updateEntry.getKey(), updateEntry.getStart(), updateEntry.getStop(), user);
                        success("Entry saved.");
                    } catch (NotAuthenticatedException e) {
                        LOGGER.severe("Can not update entry " + updateEntry.getKey() + ": " + e.getMessage());
                        error("Can not save entry. Try again.");
                    }
                }
            };
            entryForm.add(new HiddenField("key"));
            entryForm.add(new DateTimeField("start"));
            entryForm.add(new DateTimeField("stop"));
            //TODO add TimeEntry as model
            add(entryForm);
        } catch (NotAuthenticatedException e) {
            LOGGER.severe("Not authenticated: " + e.getMessage());
        }
    }
}
