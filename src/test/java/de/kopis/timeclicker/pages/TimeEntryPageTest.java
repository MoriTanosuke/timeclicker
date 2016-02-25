package de.kopis.timeclicker.pages;

import org.junit.Test;

public class TimeEntryPageTest extends AbstractWicketTestCase {

    @Test
    public void pageRendersSuccessfully() {
        tester.startPage(TimeEntryPage.class);
        tester.assertRenderedPage(TimeEntryPage.class);
    }

}