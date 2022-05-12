package com.example.livesapp.presenter.Music;

public interface IMusicProgress {
    /**
     * p层给view层的调用接口
     */
    int getCurMusicPosition();
    int getCurMusicDuration();
    void seekTo(int position);

    /**
     * p层定义好给view层实现的接口
     */
    interface ProgressUpdater{
        void updateCurMusicPosition(int currentPosition);
    }
    interface MusicDurationUpdater {
        void updateCurMusicDuration(int currentDuration);
    }

    void registDurationUpdater(IMusicProgress.MusicDurationUpdater updater);
    void unregistDurationUpdater(IMusicProgress.MusicDurationUpdater updater);
    void registProgressUpdater(IMusicProgress.ProgressUpdater updater);
    void unregistProgressUpdater(IMusicProgress.ProgressUpdater updater);
}
