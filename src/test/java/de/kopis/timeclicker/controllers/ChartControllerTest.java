package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.utils.TimeclickerEntityFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ChartControllerTest {
  @Autowired
  private MockMvc client;

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

    // create an entry
    final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    final UserService userService = UserServiceFactory.getUserService();
    User currentUser = userService.getCurrentUser();
    final Entity entry = TimeclickerEntityFactory.createTimeEntryEntity(currentUser);
    entry.setProperty("start", Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
    ds.put(entry);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void getWeeklyChart() throws Exception {
    client.perform(MockMvcRequestBuilders.get("/charts/weekly"))
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
  }

  @Test
  public void getDailyChart() throws Exception {
    client.perform(MockMvcRequestBuilders.get("/charts/daily"))
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
  }

  @Test
  public void getTagChart() throws Exception {
    client.perform(MockMvcRequestBuilders.get("/charts/tag"))
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
  }
}