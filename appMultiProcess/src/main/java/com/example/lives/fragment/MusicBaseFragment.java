package com.example.lives.fragment;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lives.app.MyApp;
import com.example.lives.beans.MusicData;
import com.example.lives.service.IMusicBinder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MusicBaseFragment extends Fragment {

    private static final String TAG = "MusicBaseFragment";
    private static final AtomicBoolean hasGottenMusicBinder = new AtomicBoolean(false);
    static IMusicBinder musicBinder;
    static List<MusicData> musicDataList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMusicBinder();
    }

    private void getMusicBinder() {
        Log.i(TAG, "getMusicBinder");
        synchronized (hasGottenMusicBinder) {
            if (hasGottenMusicBinder.get()) return;
            hasGottenMusicBinder.set(true);
        }
        realGetMusicBinder();
    }
    private void realGetMusicBinder() {
        Log.i(TAG, " realGetMusicBinder");
        //        通过异步调用+回调，获取全局MusicBinder
        MyApp.MusicServiceProxy.addListener(new MyApp.MusicServiceProxy.GetMusicServiceListener() {
            @Override
            public void workAfterMusicBinderReady(IMusicBinder musicBinder, List<MusicData> musicDataList) {
                MusicBaseFragment.musicBinder = musicBinder;
                MusicBaseFragment.musicDataList = musicDataList;
            }
        });
        MyApp.MusicServiceProxy.getMusicBinderAsync();
    }
}
