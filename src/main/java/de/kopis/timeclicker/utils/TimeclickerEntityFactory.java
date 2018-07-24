package de.kopis.timeclicker.utils;

import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.UserSettings;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;

public class TimeclickerEntityFactory {
  /**
   * Creates a {@link TimeEntry} from the given datastore {@link Entity}.
   *
   * @param timeEntryEntity already received datastore entity
   * @return a fully set up {@link TimeEntry}
   */
  public static TimeEntry buildTimeEntryFromEntity(Entity timeEntryEntity) {
    final TimeEntry entry = new TimeEntry();
    entry.setKey(KeyFactory.keyToString(timeEntryEntity.getKey()));
    if (checkProperty(timeEntryEntity, TimeEntry.ENTRY_START)) {
      entry.setStart(((Date) timeEntryEntity.getProperty(TimeEntry.ENTRY_START)).toInstant());
    }
    if (checkProperty(timeEntryEntity, TimeEntry.ENTRY_STOP)) {
      entry.setStop(((Date) timeEntryEntity.getProperty(TimeEntry.ENTRY_STOP)).toInstant());
    }
    if (checkProperty(timeEntryEntity, TimeEntry.ENTRY_BREAK_DURATION)) {
      entry.setBreakDuration(Duration.of((Long) timeEntryEntity.getProperty(TimeEntry.ENTRY_BREAK_DURATION), ChronoUnit.MILLIS));
    }
    if (checkProperty(timeEntryEntity, TimeEntry.ENTRY_DESCRIPTION)) {
      entry.setDescription((String) timeEntryEntity.getProperty(TimeEntry.ENTRY_DESCRIPTION));
    }
    if (checkProperty(timeEntryEntity, TimeEntry.ENTRY_TAGS)) {
      entry.setTags((String) timeEntryEntity.getProperty(TimeEntry.ENTRY_TAGS));
    }
    if (checkProperty(timeEntryEntity, TimeEntry.ENTRY_PROJECT)) {
      entry.setProject((String) timeEntryEntity.getProperty(TimeEntry.ENTRY_PROJECT));
    }
    return entry;
  }

  /**
   * Create a new {@link TimeEntry} for the given user.
   *
   * @param user
   * @return a {@link TimeEntry} with property <code>start</code> set to current date
   */
  public static Entity createTimeEntryEntity(@NotNull User user) {
    if (user == null) throw new IllegalArgumentException("No user provided, can not create entity");

    Entity timeEntryEntity = new Entity("TimeEntry");
    timeEntryEntity.setProperty(TimeEntry.ENTRY_START, new Date());
    // set stop=null to make if queriable
    timeEntryEntity.setProperty(TimeEntry.ENTRY_STOP, null);
    timeEntryEntity.setProperty(TimeEntry.ENTRY_USER_ID, user.getUserId());
    return timeEntryEntity;
  }

  public static UserSettings buildUserSettingsFromEntity(Entity userSettingsEntity) {
    final UserSettings us = new UserSettings();
    if (userSettingsEntity != null) {
      us.setKey(KeyFactory.keyToString(userSettingsEntity.getKey()));
      if (checkProperty(userSettingsEntity, UserSettings.TIMEZONE)) {
        us.setTimezone(TimeZone.getTimeZone((String) userSettingsEntity.getProperty(UserSettings.TIMEZONE)));
      }
      Locale locale;
      if (checkProperty(userSettingsEntity, UserSettings.LANGUAGE) && checkProperty(userSettingsEntity, UserSettings.COUNTRY)) {
        if (checkProperty(userSettingsEntity, UserSettings.VARIANT)) {
          locale = new Locale((String) userSettingsEntity.getProperty(UserSettings.LANGUAGE),
              (String) userSettingsEntity.getProperty(UserSettings.COUNTRY),
              (String) userSettingsEntity.getProperty(UserSettings.VARIANT));
        } else {
          locale = new Locale((String) userSettingsEntity.getProperty(UserSettings.LANGUAGE),
              (String) userSettingsEntity.getProperty(UserSettings.COUNTRY));
        }
      } else {
        locale = Locale.getDefault();
      }
      us.setLocale(locale);

      if (checkProperty(userSettingsEntity, UserSettings.WORKING_DURATION_PER_DAY)) {
        // set configured workingduration
        final Duration workingDurationPerDay = Duration.of((long) userSettingsEntity.getProperty(UserSettings.WORKING_DURATION_PER_DAY), ChronoUnit.MILLIS);
        us.setWorkingDurationPerDay(workingDurationPerDay);
      }

      if (checkProperty(userSettingsEntity, UserSettings.BREAK_DURATION_PER_DAY)) {
        // set configured breakduration
        final Duration breakDurationPerDay = Duration.of((long) userSettingsEntity.getProperty(UserSettings.BREAK_DURATION_PER_DAY), ChronoUnit.MILLIS);
        us.setBreakDurationPerDay(breakDurationPerDay);
      }
    }
    return us;
  }

  public static void updateUserSettingsEntity(User user, Entity entity, UserSettings settings) {
    entity.setProperty(UserSettings.TIMEZONE, settings.getTimezone().getID());
    entity.setProperty(UserSettings.WORKING_DURATION_PER_DAY, settings.getWorkingDurationPerDay().toMillis());
    entity.setProperty(UserSettings.BREAK_DURATION_PER_DAY, settings.getBreakDurationPerDay().toMillis());
    entity.setProperty(UserSettings.COUNTRY, settings.getLocale().getCountry());
    entity.setProperty(UserSettings.LANGUAGE, settings.getLocale().getLanguage());
    entity.setProperty(UserSettings.VARIANT, settings.getLocale().getVariant());
    entity.setProperty(UserSettings.USER_ID, user.getUserId());
  }

  /**
   * Checks if the given {@link Entity} has a property with the given name and
   * the property is not <code>null</code>.
   *
   * @param entity       {@link Entity} to check
   * @param propertyName property name to check for <code>not null</code>
   * @return <code>true</code> if property exists and is not <code>null</code>, else <code>false</code>
   * TODO replace with Optional
   */
  private static boolean checkProperty(Entity entity, String propertyName) {
    return entity.hasProperty(propertyName) && entity.getProperty(propertyName) != null;
  }
}
