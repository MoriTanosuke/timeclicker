package de.kopis.timeclicker.pages;

import org.junit.Test;

public class ListEntriesPageTest extends AbstractWicketTestCase {

    @Test
    public void pageRendersSuccessfully() {
        tester.startPage(ListEntriesPage.class);
        tester.assertRenderedPage(ListEntriesPage.class);
    }
}
