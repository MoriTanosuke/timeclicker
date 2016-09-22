package de.kopis.timeclicker.pages;

import java.util.Arrays;
import java.util.TimeZone;

import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.UserSettings;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.users.User;

public class UserSettingsPage extends TemplatePage {
    private UserSettings settings = new UserSettings();

    public UserSettingsPage(PageParameters parameters) {
        super("Settings", parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final User user = getCurrentUser();
        try {
            settings = getApi().getUserSettings(null, user);
            getLOGGER().info("Loaded user settings with timezone=" + settings.getTimezone().getID() + " and workingDurationPerDay=" + settings.getWorkingDurationPerDay());
        } catch (NotAuthenticatedException e) {
            getLOGGER().severe("Can not load user settings for user " + user + ": " + e.getMessage());
            error("Can not load user settings for user " + user + ": " + e.getMessage());
        } catch (EntityNotFoundException e) {
            getLOGGER().severe("No user settings found for user " + user + ", using defaults");
        }

        final IModel<String> selectedTimeZoneId = Model.of(settings.getTimezone().getID());
        final DropDownChoice<String> timezones = new DropDownChoice<>("timezones", selectedTimeZoneId, Arrays.asList(TimeZone.getAvailableIDs()));
        final TextField<Long> workingDuration = new TextField<>("workingDuration", new PropertyModel<Long>(settings, "workingDurationPerDay"));

        final Form<Void> entryForm = new Form<>("entryForm");
        entryForm.add(timezones);
        entryForm.add(workingDuration);
        entryForm.add(new Button("submit") {
            @Override
            public void onSubmit() {
                TimeZone timezone = TimeZone.getTimeZone(timezones.getModelObject());
                if (timezone == null) {
                    timezone = TimeZone.getDefault();
                    getLOGGER().fine("No timezone selected, using fallback " + timezone);
                }
                long duration = workingDuration.getModelObject();
                getLOGGER().info("Updating user settings with timezone=" + timezone);
                getLOGGER().info("Updating user settings with workingDurationPerDay=" + duration);
                try {
                    settings.setTimezone(timezone);
                    settings.setWorkingDurationPerDay(duration);
                    getApi().setUserSettings(settings, user);
                } catch (NotAuthenticatedException | EntityNotFoundException e) {
                    getLOGGER().severe("Can not save user settings: " + e.getMessage());
                }
            }
        });
        add(entryForm);
    }
}
