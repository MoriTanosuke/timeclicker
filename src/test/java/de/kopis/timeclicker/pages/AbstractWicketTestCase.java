package de.kopis.timeclicker.pages;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import de.kopis.timeclicker.WicketApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;

public class AbstractWicketTestCase {
    private final LocalServiceTestHelper userHelper =
            new LocalServiceTestHelper(new LocalUserServiceTestConfig())
                    .setEnvEmail("tester@test.org")
                    .setEnvAuthDomain("test.org")
                    .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private final LocalServiceTestHelper datastoreHelper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    protected WicketTester tester;

    @Before
    public void setUp() {
        userHelper.setUp();
        datastoreHelper.setUp();

        tester = new WicketTester(new WicketApplication());
    }

    @After
    public void tearDown() {
        //datastoreHelper.tearDown();
        userHelper.tearDown();
    }
}
