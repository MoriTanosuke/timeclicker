package de.kopis.timeclicker.api;

import de.kopis.timeclicker.exceptions.EntryNotOwnedByUserException;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.EmotionRating;
import de.kopis.timeclicker.model.TagSummary;
import de.kopis.timeclicker.model.TimeEntry;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

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

import static org.junit.Assert.*;

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
  public void testListRatings() throws NotAuthenticatedException {
    final EmotionRating rating1 = new EmotionRating(Instant.now(), EmotionRating.Emotion.GOOD);
    api.rate(rating1, user);
    final EmotionRating rating2 = new EmotionRating(Instant.now(), EmotionRating.Emotion.NEUTRAL);
    api.rate(rating2, user);

    final List<EmotionRating> ratings = api.listRatings(100, 0, user);
    assertEquals(2, ratings.size());
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

  @Test
  public void testListAll() throws Exception {
    final TimeEntry entry1 = new TimeEntry(Instant.ofEpochSecond(0), Instant.ofEpochSecond(60 * 60));
    entry1.setTags(UUID.randomUUID().toString());
    final TimeEntry entry2 = new TimeEntry(Instant.ofEpochSecond(2 * 60 * 60), Instant.ofEpochSecond(3 * 60 * 60));
    entry2.setTags(UUID.randomUUID().toString());

    Arrays.asList(entry1, entry2).forEach(e ->
        {
          try {
            api.update(null,
                Date.from(e.getStart()), Date.from(e.getStart().plus(Duration.ofHours(42))),
                e.getBreakDuration().toMillis(),
                e.getDescription(),
                e.getTags(),
                e.getProject(),
                user);
          } catch (Exception ex) {
            // ignore this for this testcase
          }
        }
    );

    List<TimeEntry> listedEntries = api.list(null, null, null, user);
    assertEquals(2, listedEntries.size());
  }

  @Test
  public void testListOnlyOneTag() throws Exception {
    final TimeEntry entry1 = new TimeEntry(Instant.ofEpochSecond(0), Instant.ofEpochSecond(60 * 60));
    entry1.setTags(UUID.randomUUID().toString());
    final TimeEntry entry2 = new TimeEntry(Instant.ofEpochSecond(2 * 60 * 60), Instant.ofEpochSecond(3 * 60 * 60));
    entry2.setTags(UUID.randomUUID().toString());

    Arrays.asList(entry1, entry2).forEach(e ->
        {
          try {
            api.update(null,
                Date.from(e.getStart()), Date.from(e.getStart().plus(Duration.ofHours(42))),
                e.getBreakDuration().toMillis(),
                e.getDescription(),
                e.getTags(),
                e.getProject(),
                user);
          } catch (Exception ex) {
            // ignore this for this testcase
          }
        }
    );

    List<TimeEntry> listedEntries = api.list(entry2.getTags(), null, null, user);
    assertEquals(1, listedEntries.size());
    assertEquals(entry2.getTags(), listedEntries.get(0).getTags());
  }

  @Test
  public void testGetTagSummary() throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final TimeEntry entry = new TimeEntry(Instant.ofEpochSecond(0), Instant.ofEpochSecond(60 * 60));
    entry.setTags(UUID.randomUUID().toString());

    api.update(null,
        Date.from(entry.getStart()), Date.from(entry.getStart().plus(Duration.ofHours(42))),
        entry.getBreakDuration().toMillis(),
        entry.getDescription(),
        entry.getTags(),
        entry.getProject(),
        user);

    final Collection<TagSummary> summaries = api.getSummaryForTags(Integer.MAX_VALUE, 0, user);
    // empty tag + random tag
    assertEquals("Tags found: " + summaries, 2, summaries.size());
  }

  @Test
  public void testGetTagSummarySince() throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final TimeEntry entry = new TimeEntry(Instant.ofEpochSecond(5 * 60 * 60), Instant.ofEpochSecond(6 * 60 * 60));
    entry.setTags(UUID.randomUUID().toString());

    final Date start = Date.from(entry.getStart());
    final Date stop = Date.from(entry.getStart().plus(Duration.ofHours(42)));
    api.update(null,
        start, stop,
        entry.getBreakDuration().toMillis(),
        entry.getDescription(),
        entry.getTags(),
        entry.getProject(),
        user);

    // we try to list after the existing entry, should return without any results
    final Collection<TagSummary> summaries = api.getSummaryForTagsSince(
        Date.from(start.toInstant().plus(Duration.ofHours(7 * 24))),
        user);
    // only empty tag
    assertEquals("Tags found: " + summaries, 1, summaries.size());
  }
}
