package de.kopis.timeclicker.api;

import de.kopis.timeclicker.exceptions.EntryNotOwnedByUserException;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;

import java.time.Duration;
import java.util.Date;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TimeclickerAPITest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(),
      new LocalDatastoreServiceTestConfig());

  private TimeclickerAPI api = new TimeclickerAPI();
  private User user;

  @Before
  public void setUp() {
    helper
        .setEnvEmail("tester@localhost.localdomain")
        .setEnvAuthDomain("localhost.localdomain")
        .setEnvIsLoggedIn(true)
        // necessary to set the user ID, otherwise it is NULL
        .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"))
        .setUp();

    final UserService userService = UserServiceFactory.getUserService();
    user = userService.getCurrentUser();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testDelete() throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final TimeEntry startedEntry = api.start(user);

    // delete the entry
    api.delete(startedEntry.getKey(), user);

    // try to get it again, should return null
    final TimeEntry entry = api.show(startedEntry.getKey(), user);
    assertNull(entry);
  }

  @Test
  public void testStop() throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final TimeEntry startedEntry = api.start(user);

    final TimeEntry stoppedEntry = api.stop(startedEntry.getKey(), user);
    assertEquals(startedEntry.getKey(), stoppedEntry.getKey());
    assertNotNull(stoppedEntry.getStop());
  }

  @Test
  public void testStopLatest() throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final TimeEntry startedEntry = api.start(user);

    api.stopLatest(user);

    final TimeEntry latestEntry = api.show(startedEntry.getKey(), user);
    assertEquals(startedEntry.getKey(), latestEntry.getKey());
    assertNotNull(latestEntry.getStop());
  }

  @Test
  public void testStart() throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final TimeEntry entry = api.start(user);
    assertNotNull(entry);
    assertNotNull(entry.getKey());
    assertNotNull(entry.getStart());
    assertNull(entry.getStop());
  }

  @Test
  public void testUpdate() throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final TimeEntry entry = api.start(user);
    // update end to start+42h
    api.update(entry.getKey(),
        Date.from(entry.getStart()), Date.from(entry.getStart().plus(Duration.ofHours(42))),
        entry.getBreakDuration().toMillis(),
        entry.getDescription(),
        entry.getTags(),
        entry.getProject(),
        user);

    final TimeEntry updatedEntry = api.show(entry.getKey(), user);
    assertEquals(entry.getStart().plus(Duration.ofHours(42)), updatedEntry.getStop());
  }

  @Test
  @Ignore("not yet implemented")
  public void testLatest() {
    fail("not yet implemented");
  }

  @Test
  @Ignore("not yet implemented")
  public void testCountAvailableEntries() {
    fail("not yet implemented");
  }

  @Test
  @Ignore("not yet implemented")
  public void testCountAvailableDates() {
    fail("not yet implemented");
  }

  @Test
  @Ignore("not yet implemented")
  public void testOverallSum() {
    fail("not yet implemented");
  }

  @Test
  @Ignore("not yet implemented")
  public void testMonthlySum() {
    fail("not yet implemented");
  }

  @Test
  @Ignore("not yet implemented")
  public void testWeeklySum() {
    fail("not yet implemented");
  }

  @Test
  @Ignore("not yet implemented")
  public void testDailySum() {
    fail("not yet implemented");
  }

  @Test
  @Ignore("not yet implemented")
  public void testGetProjects() {
    fail("not yet implemented");
  }
}