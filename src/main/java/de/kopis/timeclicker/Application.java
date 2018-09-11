package de.kopis.timeclicker;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.EntryNotOwnedByUserException;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.UserSettings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.TimeZone;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(scanBasePackages = {"de.kopis.timeclicker"})
public class Application implements WebMvcConfigurer {
  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  // TODO get locale from user settings
  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
  private static CachedSetting<TimeZone> timezone = new CachedSetting<>(TimeZone.getDefault());
  private static CachedSetting<Locale> locale = new CachedSetting<>(Locale.getDefault());

  public static DateFormat getDateFormat() {
    update();

    final DateFormat sdf = new SimpleDateFormat(DATE_PATTERN, locale.get());
    sdf.setTimeZone(timezone.get());

    return sdf;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private static void update() {
    if (!timezone.isValid() || !locale.isValid()) {
      UserService userService = UserServiceFactory.getUserService();
      User user = userService.getCurrentUser();
      TimeclickerAPI api = new TimeclickerAPI();
      try {
        UserSettings settings = api.getUserSettings(null, user);
        timezone = new CachedSetting<>(settings.getTimezone(), Duration.ofSeconds(10));
        locale = new CachedSetting<>(settings.getLocale(), Duration.ofSeconds(10));
      } catch (NotAuthenticatedException | EntryNotOwnedByUserException e) {
        // fallback to default
        timezone = new CachedSetting<>(TimeZone.getDefault());
        locale = new CachedSetting<>(Locale.getDefault());
        LOGGER.warn("Using default timezone {}, default locale {}",
            timezone.get().getDisplayName(),
            locale.get().getDisplayName(),
            e.getMessage());
      }
    } else {
      LOGGER.debug("Timezone {} is valid until {}, locale {} is valid until {}",
          timezone.get().getDisplayName(), timezone.getValidUntil(),
          locale.get().getDisplayName(), locale.getValidUntil());
    }
  }
}

class CachedSetting<T> {
  private T t;
  private final Instant validUntil;

  CachedSetting(T t) {
    this(t, Instant.now().minusSeconds(1));
  }

  CachedSetting(T t, Duration validDuration) {
    this.t = t;
    this.validUntil = Instant.now().plus(validDuration);
  }

  CachedSetting(T t, Instant validUntil) {
    this.t = t;
    this.validUntil = validUntil;
  }

  public T get() {
    return t;
  }

  public boolean isValid() {
    return Instant.now().isBefore(validUntil);
  }

  public Instant getValidUntil() {
    return validUntil;
  }
}