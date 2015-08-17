package com.spotify.heroic.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.spotify.heroic.common.DateRange;

public class DateRangeTest {
    @Test
    public void testRounded() {
        final DateRange range = new DateRange(0, 1100);
        assertEquals(new DateRange(0, 1000), range.rounded(1000));
    }

    @Test
    public void testDoesNotWrapAround() {
        final DateRange range = new DateRange(0, 0);
        assertEquals(range, range.rounded(100));
    }

    @Test
    public void testRoundedIgnoreBadArgument() {
        final DateRange range = new DateRange(0, 0);
        assertTrue(range == range.rounded(-1));
    }
}