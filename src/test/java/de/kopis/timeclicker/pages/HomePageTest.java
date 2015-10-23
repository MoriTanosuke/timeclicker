package de.kopis.timeclicker.pages;

import org.junit.Test;

public class HomePageTest extends AbstractWicketTestCase {

    @Test
    public void pageRendersSuccessfully() {
        tester.startPage(HomePage.class);
        tester.assertRenderedPage(HomePage.class);
    }
}
