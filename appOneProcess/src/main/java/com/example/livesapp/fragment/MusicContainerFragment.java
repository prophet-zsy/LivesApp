package com.example.livesapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.livesMultiProcess.R;
import com.example.livesapp.activity.MusicPlayerActivity;
import com.example.livesapp.adapter.MusicTabPagerAdapter;
import com.example.livesapp.model.beans.MusicData;
import com.example.livesapp.presenter.Music.IMusicCommonControl;
import com.example.livesapp.presenter.Music.IMusicPresenter;
import com.example.livesapp.presenter.Music.MusicPresenterProxy;
import com.example.livesapp.widget.PlayerTab;
import com.google.android.material.tabs.TabLayout;

public class MusicContainerFragment extends Fragment {

    private static final String TAG = "MusicContainerFragment";

    private PlayerTab playerTab;
    private IMusicPresenter musicPresenter;
    private UpdaterManager updaterManager;

    public static MusicContainerFragment newInstance() {
        Log.i(TAG, "newInstance");
        MusicContainerFragment fragment = new MusicContainerFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        musicPresenter = MusicPresenterProxy.getProxy(new Class[]{IMusicCommonControl.class});
        updaterManager = new UpdaterManager();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = initViewPager();
        registPlayerTabListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updaterManager.registerUpdater();
        prepareView(); // 重返页面时，更新下信息
    }

    @Override
    public void onPause() {
        super.onPause();
        updaterManager.unregisterUpdater();
    }

    private void prepareView() {
        if (musicPresenter.getMusicInfo() != null && playerTab != null) {
            playerTab.setMusicInfo(musicPresenter.getMusicInfo());
            playerTab.setStatePlay(musicPresenter.getPlayState());
        }
    }

    private View initViewPager() {
        Log.i(TAG, "initViewPager");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.music_container, null);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.musicContainer);
        playerTab = view.findViewById(R.id.player_tab);

        // getChildFragmentManager()用于fragment中动态添加fragment,而activity中动态添加fragment则使用getFragmentManager()
        viewPager.setAdapter(new MusicTabPagerAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    private void registPlayerTabListener(){
        playerTab.setLastMusicOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPresenter.lastMusic();
            }
        });
        playerTab.setPlayOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicPresenter.getPlayState() == IMusicCommonControl.PLAY_STATE.PLAYING)
                    musicPresenter.pauseMusic();
                else if (musicPresenter.getPlayState() == IMusicCommonControl.PLAY_STATE.PAUSING)
                    musicPresenter.playMusic();
            }
        });
        playerTab.setNextMusicOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPresenter.nextMusic();
            }
        });
        playerTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                跳转到播放界面
                Intent intentActivity = new Intent(getActivity(), MusicPlayerActivity.class);
                startActivity(intentActivity);
            }
        });
    }

    private class UpdaterManager{
        IMusicCommonControl.MusicInfoUpdater musicInfoUpdater = new IMusicCommonControl.MusicInfoUpdater() {
            @Override
            public void updateMusicInfo(MusicData musicData) {
                playerTab.setMusicInfo(musicData);
            }
        };
        IMusicCommonControl.MusicStateUpdater musicStateUpdater = new IMusicCommonControl.MusicStateUpdater() {
            @Override
            public void updatePlayState(IMusicCommonControl.PLAY_STATE playState) {
                Log.d(TAG, "updatePlayState() called with: playState = [" + playState + "]");
                playerTab.setStatePlay(playState);
            }
        };
        public void registerUpdater() {
            musicPresenter.registMusicInfoUpdater(musicInfoUpdater);
            musicPresenter.registMusicStateUpdater(musicStateUpdater);
        }
        public void unregisterUpdater() {
            musicPresenter.unregistMusicInfoUpdater(musicInfoUpdater);
            musicPresenter.unregistMusicStateUpdater(musicStateUpdater);
        }
    }
}
