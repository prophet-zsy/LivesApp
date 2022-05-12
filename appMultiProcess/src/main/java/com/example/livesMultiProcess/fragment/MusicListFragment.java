package com.example.livesMultiProcess.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.livesMultiProcess.R;
import com.example.livesMultiProcess.activity.MusicPlayerActivity;
import com.example.livesMultiProcess.adapter.MusicListAdapter;
import com.example.livesMultiProcess.app.MyApp;
import com.example.livesMultiProcess.beans.MusicData;
import com.example.livesMultiProcess.service.IMusicBinder;
import com.example.livesMultiProcess.widget.ListViewSideBar;
import com.example.livesMultiProcess.widget.PlayerTab;

import java.util.List;

public class MusicListFragment extends MusicBaseFragment {
    private static final String TAG = "MusicListFragment";
    //    private MusicDBHelper musicDBHelper;

    //    UI部分onCreateView后初始化
    private View inflateView;
    private ListView listView;
    private PlayerTab playerTab;
    private TextView textDialog;
    private ListViewSideBar listViewSideBar;

    private int curMusicId = -1;
    private boolean isPlaying = false;

    private DataLoaderThread thread;
    class DataLoaderThread extends Thread {

        @Override
        public void run() {

        }
    }

    public static MusicListFragment newInstance() {
        Log.i(TAG, "newInstance");
        return new MusicListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        prepareView(inflater);
        configData();
        registerListener();
        return inflateView;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        Log.i(TAG, "resumeData");
        if (checkMusicBinder()) {
            try {
                curMusicId = musicBinder.getCurMusicId();
                if (curMusicId != -1) {
                    isPlaying = musicBinder.isPlaying();
                    playerTab.setMusicInfo(musicDataList.get(curMusicId));
                    playerTab.setStatePlay(isPlaying);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkMusicBinder() {
        if (musicBinder == null) {
            Toast.makeText(getContext(), "失去与MusicService的连接", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    private void prepareView(LayoutInflater inflater) {
        inflateView = inflater.inflate(R.layout.music_list, null, false);
        listView = inflateView.findViewById(R.id.list_view);
        textDialog = inflateView.findViewById(R.id.textDialog);
        listViewSideBar = inflateView.findViewById(R.id.ListViewSideBar);
        playerTab = inflateView.findViewById(R.id.player_tab);
    }

    private void configData() {
        thread = new DataLoaderThread();
//        musicDBHelper = new MusicDBHelper(getContext());
//        for (int i = 0; i < 20; i++) {
//            musicDBHelper.insert(new MusicData("成都" + i, "赵雷", ""));
//        }
//        musicDataList = musicDBHelper.getQueryAll(MusicData.class);

        if (musicDataList == null) {  // 如果还没获取到结果，通过监听的方式来设置adapter
            MyApp.MusicServiceProxy.addListener(new MyApp.MusicServiceProxy.GetMusicServiceListener() {
                @Override
                public void workAfterMusicBinderReady(IMusicBinder musicBinder, List<MusicData> musicDataList) {
                    Activity context = getActivity();
                    if (context != null)
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BaseAdapter adapter = new MusicListAdapter(musicDataList, getActivity());
                                listView.setAdapter(adapter);
                            }
                        });
                }
            });
        } else {
            BaseAdapter adapter = new MusicListAdapter(musicDataList, getActivity());
            listView.setAdapter(adapter);
        }
    }

    private void registerListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(checkMusicBinder()) {
                    try {
                        curMusicId = position;
                        musicBinder.playMusic(curMusicId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    playerTab.setMusicInfo(musicDataList.get(curMusicId));
                    playerTab.setStatePlay(isPlaying = true);
                }
            }
        });

        listViewSideBar.setTextDialog(textDialog);

        playerTab.setLastMusicOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMusicBinder()) {
                    try {
                        curMusicId = musicBinder.lastMusic();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    playerTab.setMusicInfo(musicDataList.get(curMusicId));
                    playerTab.setStatePlay(isPlaying = true);
                }
            }
        });
        playerTab.setPlayOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMusicBinder() && curMusicId != -1) {  // 如果连接了MusicService且选中了音乐
                    try {
                        if (musicBinder.isPlaying()) {
                            playerTab.setStatePlay(isPlaying = false);
                            musicBinder.pauseMusic();
                        } else {
                            playerTab.setStatePlay(isPlaying = true);
                            musicBinder.playMusic(curMusicId);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    playerTab.setMusicInfo(musicDataList.get(curMusicId));  // 再更新下当前歌曲信息
                }
            }
        });
        playerTab.setNextMusicOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMusicBinder()) {
                    try {
                        curMusicId = musicBinder.nextMusic();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    playerTab.setMusicInfo(musicDataList.get(curMusicId));
                    playerTab.setStatePlay(isPlaying = true);
                }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
//        musicDBHelper.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        musicDBHelper.clear();
    }
}
