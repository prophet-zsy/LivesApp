package com.example.livesapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.livesMultiProcess.R;
import com.example.livesapp.activity.MainActivity;
import com.example.livesapp.presenter.sport.StepGoalPresenter;
import com.example.livesapp.presenter.sport.StepPresenter;


public class StepService extends Service {

    public static StepService ins;
    private StepPresenter stepPresenter;
    private StepGoalPresenter stepGoalPresenter;

    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private NotificationChannel notificationChannel;
    private int notificationId = 10;


    @Override
    public void onCreate() {
        super.onCreate();
        ins = this;
        initData();
        initNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new StepBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void initData() {
        stepPresenter = StepPresenter.getInstance();
        stepGoalPresenter = StepGoalPresenter.getInstance();
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
                .setContentText("今日步数 " + stepPresenter.getCurStepNum() + "步")
                .setSmallIcon(R.mipmap.walking)
                .setWhen(System.currentTimeMillis())
//                .setOngoing(true)  // 前台service，只要service运行，默认notification常驻，不需要这个；普通的notification如果要常驻需要这个
                .build();

        startForeground(notificationId, notification);  // 前台service，使用方法和普通service一样，只是需要在service中准备一个通知，调用该方法进行显示
//        notificationManager.notify(notificationId, notification);
    }

    public void remindNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.walking)
                .setWhen(System.currentTimeMillis())
                .setOngoing(false);

        int curStepNum = stepPresenter.getCurStepNum();
        int goal = stepGoalPresenter.getGoal();
        if (curStepNum >= goal) {
            notificationBuilder
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText("今日步数 " + curStepNum + "步");
        } else {
            notificationBuilder
                    .setContentTitle("今日步数 " + curStepNum + "步")
                    .setContentText("距离目标还差" + (goal - curStepNum) + "步，加油！");
        }
        Notification notification = notificationBuilder.build();
//        这里的震动需要通过channel来进行设置，高版本下，通过builder设置震动等属性的接口被弃用了
//        notificationChannel.setVibrationPattern(new long[] {Notification.DEFAULT_VIBRATE});
        if (notificationManager != null)
            notificationManager.notify(notificationId, notification);
    }

    public class StepBinder extends Binder {
        public StepService getService() {
            return StepService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

