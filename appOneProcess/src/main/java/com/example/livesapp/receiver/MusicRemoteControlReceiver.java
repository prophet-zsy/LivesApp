package com.example.livesapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.livesapp.presenter.Music.IMusicCommonControl;
import com.example.livesapp.presenter.Music.IMusicPresenter;
import com.example.livesapp.presenter.Music.MusicPresenterProxy;

public class MusicRemoteControlReceiver extends BroadcastReceiver {

//    用于远程控制的 intent 逻辑动作名
    public static final String LAST_MUSIC = "lastMusic";
    public static final String PLAY_MUSIC = "playMusic";
    public static final String NEXT_MUSIC = "nextMusic";

    private IMusicPresenter musicPresenter;

    public MusicRemoteControlReceiver() {
        musicPresenter = MusicPresenterProxy.getProxy(new Class[]{IMusicCommonControl.class});
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case LAST_MUSIC:
                musicPresenter.lastMusic();
                break;
            case PLAY_MUSIC:
                if (musicPresenter.getPlayState() == IMusicCommonControl.PLAY_STATE.PLAYING)
                    musicPresenter.pauseMusic();
                else if (musicPresenter.getPlayState() == IMusicCommonControl.PLAY_STATE.PAUSING)
                    musicPresenter.playMusic();
                break;
            case NEXT_MUSIC:
                musicPresenter.nextMusic();
                break;
        }
    }
}
