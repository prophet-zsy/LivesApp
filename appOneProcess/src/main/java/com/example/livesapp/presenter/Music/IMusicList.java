package com.example.livesapp.presenter.Music;

import com.example.livesapp.model.beans.MusicData;

import java.util.List;


/**
 * p层给view层的调用接口
 */

public interface IMusicList {
    List<MusicData> getMusicList();

    /**
     * p层定义好给view层实现的接口
     */
    interface MusicListUpdater{
        void updateMusicList(List<MusicData> musicDataList);
    }

    void registMusicListUpdater(IMusicList.MusicListUpdater updater);
    void unregistMusicListUpdater(IMusicList.MusicListUpdater updater);
}
