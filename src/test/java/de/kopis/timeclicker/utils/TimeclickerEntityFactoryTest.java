package de.kopis.timeclicker.utils;

import de.kopis.timeclicker.model.TimeEntry;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TimeclickerEntityFactoryTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(),
      new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper
        .setEnvEmail("tester@localhost.localdomain")
        .setEnvAuthDomain("localhost.localdomain")
        .setEnvIsLoggedIn(true)
        // necessary to set the user ID, otherwise it is NULL
        .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"))
        .setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void createTimeEntryEntityForUser() {
    final UserService userService = UserServiceFactory.getUserService();
    final User user = userService.getCurrentUser();

    final Entity entity = TimeclickerEntityFactory.createTimeEntryEntity(user);
    assertEquals(user.getUserId(), entity.getProperty(TimeEntry.ENTRY_USER_ID));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTimeEntryEntityWithoutUserThrowsException() {
    TimeclickerEntityFactory.createTimeEntryEntity(null);
    fail("No exception thrown, expected " + IllegalArgumentException.class);
  }

  @Test
  public void buildTimeEntryFromEntity() {
    final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
    final UserService userService = UserServiceFactory.getUserService();
    final User user = userService.getCurrentUser();

    final Entity entity = TimeclickerEntityFactory.createTimeEntryEntity(user);
    final String entityKey = KeyFactory.keyToString(datastoreService.put(entity));

    final TimeEntry entry = TimeclickerEntityFactory.buildTimeEntryFromEntity(entity);
    assertEquals(entityKey, entry.getKey());
    assertNotNull(entry.getStart());
    assertNull(entry.getStop());
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildTimeEntryFromNull() {
    TimeclickerEntityFactory.buildTimeEntryFromEntity(null);
    fail("No exception thrown, expected " + IllegalArgumentException.class);
  }
}