package com.example.lives.utils;

public class TimeUtil {
    public static String minutesToHHMM(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        StringBuffer res = new StringBuffer();
        res.append(hours);
        res.append(":");
        res.append(mins);
        return res.toString();
    }
}
