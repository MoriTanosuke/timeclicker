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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(scanBasePackages = {"de.kopis.timeclicker"})
public class Application implements WebMvcConfigurer {
  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  private static final Duration DEFAULT_CACHE_DURATION = Duration.ofSeconds(10);
  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
  private static CachedSetting<UserSettings> settings;

  public static DateFormat getDateFormat() {
    update();

    final DateFormat sdf = new SimpleDateFormat(DATE_PATTERN, settings.get().getLocale());
    sdf.setTimeZone(settings.get().getTimezone());

    return sdf;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private static void update() {
    if (settings == null || !settings.isValid()) {
      UserService userService = UserServiceFactory.getUserService();
      User user = userService.getCurrentUser();
      TimeclickerAPI api = new TimeclickerAPI();
      try {
        UserSettings s = api.getUserSettings(null, user);
        settings = new CachedSetting<>(s, DEFAULT_CACHE_DURATION);
      } catch (NotAuthenticatedException | EntryNotOwnedByUserException e) {
        // fallback to default
        LOGGER.warn("Settings not valid, using defaults",
            e.getMessage());
      }
    } else {
      LOGGER.debug("Timezone {}, locale {} - valid until {}",
          settings.get().getTimezone().getDisplayName(), settings.get().getLocale().getDisplayName(),
          settings.getValidUntil());
    }
  }
}

class CachedSetting<T> {
  private final T t;
  private final Instant validUntil;

  CachedSetting(T t) {
    this(t, Duration.ZERO);
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