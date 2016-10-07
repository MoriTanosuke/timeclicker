package de.kopis.timeclicker.pages;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.users.User;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.UserSettings;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;

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
            getLOGGER().info("Loaded user settings with timezone=" + settings.getTimezone().getID()
                    + " locale=" + settings.getLocale().getDisplayName()
                    + " workingDurationPerDay=" + settings.getWorkingDurationPerDay());
        } catch (NotAuthenticatedException e) {
            getLOGGER().severe("Can not load user settings for user " + user + ": " + e.getMessage());
            error("Can not load user settings for user " + user + ": " + e.getMessage());
        } catch (EntityNotFoundException e) {
            getLOGGER().severe("No user settings found for user " + user + ", using defaults");
        }
        Locale[] availableLocales = Locale.getAvailableLocales();
        Arrays.sort(availableLocales, new Comparator<Locale>() {
            @Override
            public int compare(Locale o1, Locale o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        final IModel<String> selectedTimeZoneId = Model.of(settings.getTimezone().getID());
        final IModel<Locale> selectedLocale = Model.of(settings.getLocale());
        final DropDownChoice<String> timezones = new DropDownChoice<>("timezones", selectedTimeZoneId, Arrays.asList(TimeZone.getAvailableIDs()));
        final DropDownChoice<Locale> locales = new DropDownChoice<>("locales", selectedLocale, Arrays.asList(availableLocales), new IChoiceRenderer<Locale>() {
            @Override
            public String getDisplayValue(Locale object) {
                return object.getDisplayName();
            }

            @Override
            public String getIdValue(Locale object, int index) {
                return object.getDisplayName();
            }
        });
        final TextField<Long> workingDuration = new TextField<>("workingDuration", new PropertyModel<Long>(settings, "workingDurationPerDay"));

        final Form<Void> entryForm = new Form<>("entryForm");
        entryForm.add(timezones);
        entryForm.add(locales);
        entryForm.add(workingDuration);
        entryForm.add(new Button("submit") {
            @Override
            public void onSubmit() {
                TimeZone timezone = TimeZone.getTimeZone(timezones.getModelObject());
                if (timezone == null) {
                    timezone = TimeZone.getDefault();
                    getLOGGER().fine("No timezone selected, using fallback " + timezone);
                }
                Locale locale = locales.getModelObject();
                if (locale == null) {
                    locale = Locale.getDefault();
                }
                long duration = workingDuration.getModelObject();
                getLOGGER().info("Updating user settings with timezone=" + timezone
                        + " locale=" + locale
                        + " workingDurationPerDay=" + duration);
                try {
                    settings.setTimezone(timezone);
                    settings.setLocale(locale);
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
