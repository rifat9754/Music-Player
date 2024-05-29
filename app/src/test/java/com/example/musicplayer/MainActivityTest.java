package com.example.musicplayer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;



public class MainActivityTest {

    @Test
    public void testGetReadableTime_OnlySeconds() {
        assertEquals("0:5", MainActivity.getReadableTime(5000));
    }


    @Test
    public void testGetReadableTime_OnlyMinutes() {
        assertEquals("1:0", MainActivity.getReadableTime(60000));
    }

    @Test
    public void testGetReadableTime_MinutesAndSeconds() {
        assertEquals("2:30", MainActivity.getReadableTime(150000));
    }

    @Test
    public void testGetReadableTime_HourAndMore() {
        assertEquals("1:1:30", MainActivity.getReadableTime(3690000));
    }

    @Test
    public void testGetReadableTime_HoursOnly() {
        assertEquals("1:0:0", MainActivity.getReadableTime(3600000));
    }

    @Test
    public void testGetReadableTime_ComplexTime() {
        assertEquals("2:16:40", MainActivity.getReadableTime(8200000));
}

}