package com.example.livesapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.example.livesMultiProcess.R;
import com.example.livesapp.activity.MusicPlayerActivity;
import com.example.livesapp.model.beans.MusicData;
import com.example.livesapp.presenter.Music.IMusicCommonControl;
import com.example.livesapp.presenter.Music.IMusicPresenter;
import com.example.livesapp.presenter.Music.IMusicProgress;
import com.example.livesapp.presenter.Music.MusicPresenterProxy;
import com.example.livesapp.receiver.MusicRemoteControlReceiver;
import com.example.livesapp.widget.PlayerTab;


/**
 * 前台service，运行音乐服务
 */


public class MusicForegroundService extends Service {
    private static final String TAG = "MusicForegroundService";

    //  ForegroundService只使用了IMusicCommonControl和IMusicProgress下的功能
    public static MusicForegroundService ins;
    private IMusicPresenter musicPresenter;
    private NotificationManager mNotificationManager;
    private Notification notification;
    private int notificationId = 1;
    private RemoteViews remoteViews;
    private UpdaterManager updaterManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: " + ins);
        super.onCreate();
        ins = this;
        musicPresenter = MusicPresenterProxy.getProxy(new Class[]{IMusicCommonControl.class, IMusicProgress.class});
        updaterManager = new UpdaterManager();
        prepareNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ins = null;
        stopForeground(true);
        updaterManager.unregisterUpdater();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        startForeground(notificationId, notification);
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(notificationId, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    private void prepareNotification() {
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String channel = "channel_1";
        NotificationChannel channelBody = new NotificationChannel(channel, "消息推送", NotificationManager.IMPORTANCE_MIN);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channelBody);
        remoteViews = new RemoteViews(getPackageName(), R.layout.player_tab);
        remoteViews.setViewVisibility(R.id.playProgress, View.VISIBLE);
        updaterManager.registerUpdater();
        registRemoteViewListener();
        notification = new Notification.Builder(this, channel)
                .setCustomContentView(remoteViews)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build();
    }

    public class MyBinder extends Binder {
        public MusicForegroundService getService() {
            return MusicForegroundService.this;
        }
    }

    private void registRemoteViewListener(){
        setOnClickRemoteViews(MusicRemoteControlReceiver.LAST_MUSIC, R.id.last_music);
        setOnClickRemoteViews(MusicRemoteControlReceiver.PLAY_MUSIC, R.id.play);
        setOnClickRemoteViews(MusicRemoteControlReceiver.NEXT_MUSIC, R.id.next_music);
    }

    private void setOnClickRemoteViews(String action, int viewId) {
        Intent intent = new Intent(this, MusicRemoteControlReceiver.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
    }

    private class UpdaterManager{
        IMusicCommonControl.MusicInfoUpdater musicInfoUpdater = new IMusicCommonControl.MusicInfoUpdater() {
            @Override
            public void updateMusicInfo(MusicData musicData) {
                Log.d(TAG, "updateMusicInfo: " + musicData.getName());
                PlayerTab.setMusicInfo(remoteViews, musicData);
                mNotificationManager.notify(notificationId, notification);
            }
        };
        IMusicCommonControl.MusicStateUpdater musicStateUpdater = new IMusicCommonControl.MusicStateUpdater() {
            @Override
            public void updatePlayState(IMusicCommonControl.PLAY_STATE playState) {
                Log.d(TAG, "updatePlayState() called with: playState = [" + playState + "]");
                PlayerTab.setStatePlay(remoteViews, playState);
                mNotificationManager.notify(notificationId, notification);
            }
        };
        IMusicProgress.MusicDurationUpdater musicDurationUpdater = new IMusicProgress.MusicDurationUpdater() {
            @Override
            public void updateCurMusicDuration(int currentDuration) {
                PlayerTab.setPlayProgressDuration(remoteViews, currentDuration);
                mNotificationManager.notify(notificationId, notification);
            }
        };
        IMusicProgress.ProgressUpdater progressUpdater = new IMusicProgress.ProgressUpdater() {
            @Override
            public void updateCurMusicPosition(int currentPosition) {
                PlayerTab.setPlayProgressPosition(remoteViews, currentPosition);
                mNotificationManager.notify(notificationId, notification);
            }
        };
        public void registerUpdater() {
            musicPresenter.registMusicInfoUpdater(musicInfoUpdater);
            musicPresenter.registMusicStateUpdater(musicStateUpdater);
            musicPresenter.registDurationUpdater(musicDurationUpdater);
            musicPresenter.registProgressUpdater(progressUpdater);
        }
        public void unregisterUpdater() {
            musicPresenter.unregistMusicInfoUpdater(musicInfoUpdater);
            musicPresenter.unregistMusicStateUpdater(musicStateUpdater);
            musicPresenter.unregistDurationUpdater(musicDurationUpdater);
            musicPresenter.unregistProgressUpdater(progressUpdater);
        }
    }
}
