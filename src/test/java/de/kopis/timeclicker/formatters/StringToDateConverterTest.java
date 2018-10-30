package de.kopis.timeclicker.formatters;

import java.util.Date;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringToDateConverterTest {

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
  public void testConvert() {
    final StringToDateConverter converter = new StringToDateConverter();
    Date date = converter.convert("2018-01-01 00:00:00 +0000");
    assertEquals(1514764800000L, date.getTime());
  }

}