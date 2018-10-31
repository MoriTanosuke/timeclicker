package de.kopis.timeclicker.model;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class TagSummaryTest {

    @Test
    public void testAddDurationWithEmptyTag() {
        final TagSummary tag1 = new TagSummary();
        TimeEntry t1 = new TimeEntry(Instant.ofEpochSecond(0), Instant.ofEpochSecond(21 * 60 * 60));
        tag1.add(t1);
        TimeEntry t2 = new TimeEntry(Instant.ofEpochSecond(0), Instant.ofEpochSecond(21 * 60 * 60));
        tag1.add(t2);
        assertEquals(Duration.ofHours(42), tag1.getDuration());
    }

    @Test
    public void testAddDurationWithMatchingTag() {
        final TagSummary tag1 = new TagSummary("tag1");
        TimeEntry t1 = new TimeEntry(Instant.ofEpochSecond(0), Instant.ofEpochSecond(21 * 60 * 60));
        t1.setTags("tag1,tag2");
        tag1.add(t1);
        TimeEntry t2 = new TimeEntry(Instant.ofEpochSecond(0), Instant.ofEpochSecond(21 * 60 * 60));
        t2.setTags("tag2,tag1");
        tag1.add(t2);
        assertEquals(Duration.ofHours(42), tag1.getDuration());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDurationWithoutMatchingTag() {
        final TagSummary tag1 = new TagSummary("tag1");
        TimeEntry t1 = new TimeEntry(Instant.ofEpochSecond(0), Instant.ofEpochSecond(21 * 60 * 60));
        t1.setTags("tag2, tag3");
        tag1.add(t1);
    }
}