package com.example.livesapp.presenter.Music;

import com.example.livesapp.model.beans.MusicData;

/**
 * p层给view层的调用接口
 */

public interface IMusicCommonControl {
    enum PLAY_STATE {
        DOWNLOADING(0), PLAYING(1), PAUSING(2);
        int value;
        PLAY_STATE(int val) {
            this.value = val;
        }
    }

//    用于控制
    void playMusic(int musicId);
    void playMusic();
    void pauseMusic();
    void nextMusic();
    void lastMusic();

//    用于恢复页面信息
    PLAY_STATE getPlayState();
    MusicData getMusicInfo();

    /**
     * p层定义好给view层实现的接口
     */
//    用于回调
    interface MusicInfoUpdater{
        void updateMusicInfo(MusicData musicData);
    }
    interface MusicStateUpdater{
        void updatePlayState(PLAY_STATE playState);
    }
    void registMusicInfoUpdater(IMusicCommonControl.MusicInfoUpdater updater);
    void unregistMusicInfoUpdater(IMusicCommonControl.MusicInfoUpdater updater);
    void registMusicStateUpdater(IMusicCommonControl.MusicStateUpdater updater);
    void unregistMusicStateUpdater(IMusicCommonControl.MusicStateUpdater updater);
}
