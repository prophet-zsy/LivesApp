package com.example.livesapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.livesMultiProcess.R;
import com.example.livesapp.model.beans.MusicData;
import com.example.livesapp.presenter.Music.IMusicCommonControl;
import com.example.livesapp.utils.TimeUtil;

public class PlayerTab extends RelativeLayout {

    private static final String TAG = "PlayerTab";

    TextView musicName;
    TextView musicAuthor;
    ImageView play;
    ProgressBar downloadProgress;
    ImageView lastMusic;
    ImageView nextMusic;
    boolean withProgress;
    ProgressBar playProgress;
    TextView curTime;
    TextView totalTime;

    public PlayerTab(Context context) {
        super(context);
        withProgress = false;
        init(context);
    }

    public PlayerTab(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        withProgress = attrs.getAttributeBooleanValue("app", "withProgress", false);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.player_tab, this);
        musicName = findViewById(R.id.music_name);
        musicAuthor = findViewById(R.id.music_author);
        play = findViewById(R.id.play);
        downloadProgress = findViewById(R.id.downloadProgress);
        lastMusic = findViewById(R.id.last_music);
        nextMusic = findViewById(R.id.next_music);
        playProgress = findViewById(R.id.playProgress);
        curTime = findViewById(R.id.curTime);
        totalTime = findViewById(R.id.totalTime);
        if (withProgress) {
            playProgress.setVisibility(VISIBLE);
            curTime.setVisibility(VISIBLE);
            totalTime.setVisibility(VISIBLE);
        } else {
            playProgress.setVisibility(GONE);
            curTime.setVisibility(GONE);
            totalTime.setVisibility(GONE);
        }
    }

    public void setPlayOnClickListener(View.OnClickListener listener) {
        play.setOnClickListener(listener);
    }
    public void setLastMusicOnClickListener(View.OnClickListener listener) {
        lastMusic.setOnClickListener(listener);
    }
    public void setNextMusicOnClickListener(View.OnClickListener listener) {
        nextMusic.setOnClickListener(listener);
    }

    public void setMusicInfo(MusicData musicInfo) {
        musicName.setText(musicInfo.getName());
        musicAuthor.setText(musicInfo.getAuthor());
    }

    public void setStatePlay(IMusicCommonControl.PLAY_STATE playState) {
        Log.d(TAG, "setStatePlay() called with: playState = [" + playState + "]");
        switch (playState) {
            case PLAYING:
                downloadProgress.setVisibility(View.GONE);
                play.setImageResource(R.mipmap.ic_pause);
                break;
            case PAUSING:
                downloadProgress.setVisibility(View.GONE);
                play.setImageResource(R.mipmap.ic_play);
                break;
            case DOWNLOADING:
                downloadProgress.setVisibility(View.VISIBLE);
                play.setImageResource(R.mipmap.ic_play);
                break;
        }
    }

    public void setPlayProgressPosition(int position) {
        curTime.setText(TimeUtil.minutesToHHMM(position));
        playProgress.setProgress(position);
    }

    public void setPlayProgressDuration(int duration) {
        totalTime.setText(TimeUtil.minutesToHHMM(duration));
        playProgress.setMax(duration);
    }

    /**
     * 下面的静态方法在控制通知的remoteViews的时候使用
     */
    public static void setMusicInfo(RemoteViews remoteViews, MusicData musicInfo) {
        remoteViews.setTextViewText(R.id.music_name, musicInfo.getName());
        remoteViews.setTextViewText(R.id.music_author, musicInfo.getAuthor());
    }
    public static void setStatePlay(RemoteViews remoteViews, IMusicCommonControl.PLAY_STATE playState) {
        switch (playState) {
            case PLAYING:
                remoteViews.setViewVisibility(R.id.downloadProgress, View.GONE);
                remoteViews.setImageViewResource(R.id.play, R.mipmap.ic_pause);
                break;
            case PAUSING:
                remoteViews.setViewVisibility(R.id.downloadProgress, View.GONE);
                remoteViews.setImageViewResource(R.id.play, R.mipmap.ic_play);
                break;
            case DOWNLOADING:
                remoteViews.setViewVisibility(R.id.downloadProgress, View.VISIBLE);
                remoteViews.setImageViewResource(R.id.play, R.mipmap.ic_play);
                break;
        }
    }
    public static void setPlayProgressPosition(RemoteViews remoteViews, int position) {
        remoteViews.setTextViewText(R.id.curTime, TimeUtil.minutesToHHMM(position));
        remoteViews.setInt(R.id.playProgress, "setProgress", position);
    }

    public static void setPlayProgressDuration(RemoteViews remoteViews, int duration) {
        remoteViews.setTextViewText(R.id.totalTime, TimeUtil.minutesToHHMM(duration));
        remoteViews.setInt(R.id.playProgress, "setMax", duration);
    }
}
