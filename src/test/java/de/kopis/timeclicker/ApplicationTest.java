package de.kopis.timeclicker;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.model.UserSettings;

import java.util.TimeZone;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationTest {
  private static final TimeZone USER_TIMEZONE = TimeZone.getTimeZone("Singapore");
  private static final int DEFAULT_CACHE_DURATION = 11 * 1000;
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(),
      new LocalDatastoreServiceTestConfig());

  private TimeclickerAPI api = new TimeclickerAPI();
  private User user;

  @Before
  public void setUp() throws Exception {
    helper
        .setEnvEmail("tester@localhost.localdomain")
        .setEnvAuthDomain("localhost.localdomain")
        .setEnvIsLoggedIn(true)
        // necessary to set the user ID, otherwise it is NULL
        .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"))
        .setUp();

    final UserService userService = UserServiceFactory.getUserService();
    user = userService.getCurrentUser();
    // set a custom timezone for the current user
    UserSettings settings = new UserSettings();
    settings.setTimezone(USER_TIMEZONE);
    api.setUserSettings(settings, user);
    // make sure cache is expired
    Thread.sleep(DEFAULT_CACHE_DURATION);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void formatsWithUserTimezone() throws Exception {
    // now check if user timezone is used
    assertEquals(USER_TIMEZONE, Application.getDateFormat().getTimeZone());
  }

  @Test
  public void reloadsSettingsAfter10Seconds() throws Exception {
    assertEquals(USER_TIMEZONE, Application.getDateFormat().getTimeZone());
    // set different timezone
    final UserSettings settings = api.getUserSettings(null, user);
    TimeZone newTimezone = TimeZone.getTimeZone("UTC");
    settings.setTimezone(newTimezone);
    api.setUserSettings(settings, user);
    // check if Application still caches the previous timezone
    assertEquals(USER_TIMEZONE, Application.getDateFormat().getTimeZone());
    // wait to expire
    Thread.sleep(DEFAULT_CACHE_DURATION);
    assertEquals(newTimezone, Application.getDateFormat().getTimeZone());
  }

  @Test
  public void unauthenticatedUsersUseDefaults() {
    helper.setEnvIsLoggedIn(false).setUp();
    assertEquals(TimeZone.getDefault(), Application.getDateFormat().getTimeZone());
  }
}
