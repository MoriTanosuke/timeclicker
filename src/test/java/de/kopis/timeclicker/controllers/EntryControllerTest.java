package de.kopis.timeclicker.controllers;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.utils.TimeclickerEntityFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EntryControllerTest {
  public static final SimpleDateFormat QUERY_PARAM_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  @Autowired
  private MockMvc client;

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
          new LocalUserServiceTestConfig(),
          new LocalDatastoreServiceTestConfig());

  private final String desc = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    helper
            .setEnvEmail("tester@localhost.localdomain")
            .setEnvAuthDomain("localhost.localdomain")
            .setEnvIsLoggedIn(true)
            // necessary to set the user ID, otherwise it is NULL
            .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"))
            .setUp();

    // add an entry at a specific date with UUID description to identify
    final UserService userService = UserServiceFactory.getUserService();
    final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    final User currentUser = userService.getCurrentUser();
    final Entity entry = TimeclickerEntityFactory.createTimeEntryEntity(currentUser);
    entry.setProperty(TimeEntry.ENTRY_DESCRIPTION, desc);
    // create entry last week, so we can filter it out later
    entry.setProperty(TimeEntry.ENTRY_START, Date.from(Instant.now().minus(7, ChronoUnit.DAYS)));
    // 30min long
    entry.setProperty(TimeEntry.ENTRY_STOP, Date.from(Instant.now().minus(7, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES)));
    ds.put(entry);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void listAllEntries() throws Exception {
    client.perform(MockMvcRequestBuilders.get("/entries"))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.content().string(new BaseMatcher<String>() {
              @Override
              public boolean matches(Object item) {
                return ((String) item).contains(desc);
              }

              @Override
              public void describeTo(Description description) {
                description.appendText(" does not contain entry with description ")
                        .appendValue(desc);
              }
            }));
  }

  @Test
  public void listAllEntriesStartingYesterday() throws Exception {
    final String yesterday = QUERY_PARAM_DATE_FORMAT.format(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
    client.perform(MockMvcRequestBuilders.get("/entries?from=" + yesterday))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.content().string(new BaseMatcher<String>() {
              @Override
              public boolean matches(Object item) {
                return !((String) item).contains(desc);
              }

              @Override
              public void describeTo(Description description) {
                description.appendText(" unexpectedly contains earlier entry with description ")
                        .appendValue(desc);
              }
            }));
  }

  @Test
  public void createNewEntry() throws Exception {
    final String project = UUID.randomUUID().toString();

    // create a new entry
    client.perform(MockMvcRequestBuilders.post("/entries")
              .param("project", project))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/entries"));

    // check if new entry was persisted
    client.perform(MockMvcRequestBuilders.get("/entries"))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.content().string(new BaseMatcher<String>() {
              @Override
              public boolean matches(Object item) {
                return ((String) item).contains(project);
              }

              @Override
              public void describeTo(Description description) {
                description.appendText(" does not contain entry with project ")
                        .appendValue(project);
              }
            }));
  }
}
