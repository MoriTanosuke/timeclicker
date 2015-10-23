package de.kopis.timeclicker.pages;

import org.junit.Ignore;
import org.junit.Test;

public class TimeEntryPageTest extends AbstractWicketTestCase {

    @Test @Ignore("Unable to find component with id 'entryForm'")
    public void pageRendersSuccessfully() {
        tester.startPage(TimeEntryPage.class);
        tester.assertRenderedPage(TimeEntryPage.class);
    }

}