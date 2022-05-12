package com.example.livesapp.utils;

import android.os.CountDownTimer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TimeUtil {
    public static String hoursMinutesToHHMM(int hours, int minutes) {
        DecimalFormat format = new DecimalFormat("00");
        StringBuffer res = new StringBuffer();
        res.append(format.format(hours));
        res.append(":");
        res.append(format.format(minutes));
        return res.toString();
    }
    public static String minutesToHHMM(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hoursMinutesToHHMM(hours, mins);
    }
    public static int getMinuteFromHHMM(String time) {
        return Integer.parseInt(time.substring(3,5));
    }
    public static int getHourFromHHMM(String time) {
        return Integer.parseInt(time.substring(0,2));
    }
    public static int millisSecondsToSeconds(long millisSeconds) {
        return (int) millisSeconds / 1000;
    }
    public static long secondsToMillisSeconds(int seconds) {
        return (long) seconds * 1000;
    }

    public static String getTodayDate() {
        String todayDate;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        todayDate = simpleDateFormat.format(new Date());
        return todayDate;
    }

    public static String getTimeNow() {
        String nowTime;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        nowTime = simpleDateFormat.format(new Date());
        return nowTime;
    }

    public static class CycleRun extends CountDownTimer {  // 按时间间隔，循环执行

        public interface WorkTimeUp {
            void doWork();
        }

        private WorkTimeUp workTimeUp;

        public CycleRun(long timeInterval, WorkTimeUp workTimeUp) {
            super(timeInterval, timeInterval + 1000);  // 只利用onFinish函数
            this.workTimeUp = workTimeUp;
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            cancel();
            this.workTimeUp.doWork();
            start();
        }
    }
}
