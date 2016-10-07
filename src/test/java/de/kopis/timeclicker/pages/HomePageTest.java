package de.kopis.timeclicker.pages;

import org.apache.wicket.markup.html.link.Link;
import org.junit.Test;

public class HomePageTest extends AbstractWicketTestCase {

    @Test
    public void pageRendersSuccessfully() {
        tester.startPage(HomePage.class);
        tester.assertRenderedPage(HomePage.class);
        //TODO assert that start button is visible
        tester.assertComponent("start", Link.class);
        tester.assertInvisible("stop");
    }
}
