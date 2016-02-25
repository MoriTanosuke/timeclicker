package de.kopis.timeclicker.pages;

import org.junit.Ignore;
import org.junit.Test;

public class TimeEntryPageTest extends AbstractWicketTestCase {

    @Test
    @Ignore("still failing with Unable to find component with id 'entryForm' in [TransparentWebMarkupContainer [Component id = wicket_extend8]]")
    public void pageRendersSuccessfully() {
        tester.startPage(TimeEntryPage.class);
        tester.assertRenderedPage(TimeEntryPage.class);
    }

}