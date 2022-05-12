package com.example.livesapp.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class TimeUtilTest {

    @Test
    public void getTodayDate() {
        System.out.println(TimeUtil.getTodayDate());
    }

    @Test
    public void getTimeNow() {
        System.out.println(TimeUtil.getTimeNow());
    }
}