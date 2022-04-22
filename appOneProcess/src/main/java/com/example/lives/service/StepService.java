package com.example.lives.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

import com.example.lives.R;
import com.example.lives.activity.MainActivity;
import com.example.lives.beans.StepData;
import com.example.lives.fragment.UpdateSportUiListener;
import com.example.lives.utils.DBHelper;
import com.example.lives.utils.countstep.CountStep;
import com.example.lives.utils.countstep.ListenPassStepNum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class StepService extends Service {

    private CountStep countStep;

    private String todayDate;
    //  当前统计的步数
    private int curStepNum = 0;

    private CountTimer countTimer;
    private long duration = 30 * 1000;

    private DBHelper dbHelper;

    private UpdateSportUiListener updateUi;

    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private NotificationChannel notificationChannel;
    private int notificationId = 10;

    private BroadcastReceiver broadcastReceiver;

    private String spFileName = "user";
    private SharedPreferences sp;
    private String stepNumPerDay;
    private String remind;
    private String remindTime;

    public StepService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        initNotification();
        initCountStep();
        initBroadCast();
        startCountTime();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new StepBinder();
    }

    private void startCountTime() {  // 自己计时，定期进行动作（保存、设置、提醒）
        if (countTimer == null) {
            countTimer = new CountTimer(duration, 1000);
        }
        countTimer.start();
    }

    private void initBroadCast() {  // 监听广播，触发一些动作（保存、设置、提醒）
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Intent.ACTION_SCREEN_ON:
                        duration = 30 * 1000;
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        duration = 60 * 1000;
                        break;
                    case Intent.ACTION_SHUTDOWN:
                        save();
                        break;
                    case Intent.ACTION_DATE_CHANGED: // 日期自动或手动发生变化，会发出这个广播，只有日期增大时才会广播，变成过去的日期不会发生广播
                    case Intent.ACTION_TIME_CHANGED: // 手动修改时间发出这个广播
                        save();
                        isNewDay();  // 判断是否是新的一天，如果是，进行设置
                        break;
                    case Intent.ACTION_TIME_TICK: // 每分钟发出这个广播
                        tryRemind();  // 每分钟看是否需要提醒用户锻炼
                        break;
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initData() {
        todayDate = getTodayDate();  // 更新日期
        dbHelper = dbHelper == null ? new DBHelper(this) : dbHelper;
        List<StepData> list = dbHelper.getQueryByWhere(StepData.class, "date", new String[]{getTodayDate()});
        if (list.size() == 1) {
            StepData stepData = list.get(0);
            curStepNum = Integer.parseInt(stepData.getStep().replace("步", ""));
        } else if (list.size() > 1) {
            throw new RuntimeException("multi rows query from the database");
        } else if (list.size() == 0) {
            curStepNum = 0;
        }
    }

    private void initNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "40";
        String channelName = "step";
        notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
//        notificationChannel.enableVibration(true);
//        notificationChannel.setVibrationPattern(new long[] {0, 2000, 1000, 2000, 1000, 2000, 1000});
//        跟NotificationChannel相关的属性要在创建之前进行设置才有效
        notificationManager.createNotificationChannel(notificationChannel);
        notificationBuilder = new Notification.Builder(this, channelId);

        Notification notification = notificationBuilder
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("今日步数 " + curStepNum + "步")
                .setSmallIcon(R.mipmap.walking)
                .setWhen(System.currentTimeMillis())
//                .setOngoing(true)  // 前台service，只要service运行，默认notification常驻，不需要这个；普通的notification如果要常驻需要这个
                .build();
        startForeground(notificationId, notification);  // 前台service，使用方法和普通service一样，只是需要在service中准备一个通知，调用该方法进行显示
//        notificationManager.notify(notificationId, notification);
    }

    private void updateNotificationAndUi() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = notificationBuilder
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("今日步数 " + curStepNum + "步")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.walking)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .build();

        if (notificationManager != null)
            notificationManager.notify(notificationId, notification);
        if (updateUi != null)
            updateUi.updateUi(curStepNum);
    }

    private void remindNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = notificationBuilder
                .setContentTitle("今日步数 " + curStepNum + "步")
                .setContentText("距离目标还差" + (Integer.parseInt(stepNumPerDay) - curStepNum) + "步，加油！")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.walking)
                .setWhen(System.currentTimeMillis())
                .setOngoing(false)
                .build();
//        这里的震动需要通过channel来进行设置，高版本下，通过builder设置震动等属性的接口被弃用了
//        notificationChannel.setVibrationPattern(new long[] {Notification.DEFAULT_VIBRATE});
        if (notificationManager != null)
            notificationManager.notify(notificationId, notification);
    }

    private void initCountStep() {
        countStep = new CountStep(this);
        countStep.setStep(curStepNum);  // 计步器取出的步子值开始计步
        countStep.setListenPassStepNum(new ListenPassStepNum() {
            @Override
            public void stepChanged(int steps) {
                curStepNum = steps;
//                更新步数通知
                updateNotificationAndUi();
            }
        });
    }

    public void initUpdateUi(UpdateSportUiListener updateUi) {
        this.updateUi = updateUi;
    }

    public String getTodayDate() {
        if (todayDate == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            todayDate = simpleDateFormat.format(new Date());
        }
        return todayDate;
    }

    public String getTimeNow() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(new Date());
    }

    private void tryRemind() {  //todo 每分钟读一次文件么？？？
        sp = getSharedPreferences(spFileName, MODE_PRIVATE);
        stepNumPerDay = sp.getString("stepNumPerDay", "7000");
        remind = sp.getString("remind", "0");
        remindTime = sp.getString("remindTime", "20:00");
        if ("1".equals(remind) && Integer.parseInt(stepNumPerDay) > curStepNum && getTimeNow().equals(remindTime)) {
            remindNotification();
        }
    }


    private void isNewDay() {
        if (getTimeNow().equals("00:00") && !getTodayDate().equals(todayDate)) {
            initData();
        }
    }

    private void save() {
        List<StepData> list = dbHelper.getQueryByWhere(StepData.class, "date", new String[]{todayDate});
        if (list.size() == 1) {
            StepData stepData = list.get(0);
            stepData.setStep(curStepNum + "步");
            dbHelper.update(stepData);
        } else if (list.size() == 0) {
            StepData stepData = new StepData();
            stepData.setDate(todayDate);
            stepData.setStep(curStepNum + "步");
            dbHelper.insert(stepData);
        } else if (list.size() > 1)
            throw new RuntimeException("multi rows query from the database");
    }

    public class CountTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            cancel();
            save();
            start();
        }
    }

    public int getCurStepNum() {
        return curStepNum;
    }

    public class StepBinder extends Binder {
        public StepService getService() {
            return StepService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}

