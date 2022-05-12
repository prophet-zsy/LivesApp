package com.example.livesapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.livesMultiProcess.R;
import com.example.livesapp.adapter.MusicListAdapter;
import com.example.livesapp.model.beans.MusicData;
import com.example.livesapp.presenter.Music.IMusicCommonControl;
import com.example.livesapp.presenter.Music.IMusicList;
import com.example.livesapp.presenter.Music.IMusicPresenter;
import com.example.livesapp.presenter.Music.MusicPresenterProxy;
import com.example.livesapp.widget.ListViewSideBar;

import java.util.List;

public class MusicListFragment extends Fragment {
    private static final String TAG = "MusicListFragment";
    //    private MusicDBHelper musicDBHelper;

    private IMusicPresenter musicPresenter;
    private MusicListAdapter adapter;
    private UpdaterManager updaterManager;

    //    UI部分onCreateView后初始化
    private View inflateView;
    private ListView listView;
    private TextView textDialog;
    private ListViewSideBar listViewSideBar;


    public static MusicListFragment newInstance() {
        Log.i(TAG, "newInstance");
        return new MusicListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicPresenter = MusicPresenterProxy.getProxy(new Class[]{IMusicCommonControl.class, IMusicList.class});
        Log.i(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        prepareView(inflater);
        configTools();
        registerListener();
        return inflateView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void prepareView(LayoutInflater inflater) {
        inflateView = inflater.inflate(R.layout.music_list, null, false);
        listView = inflateView.findViewById(R.id.list_view);
        textDialog = inflateView.findViewById(R.id.textDialog);
        listViewSideBar = inflateView.findViewById(R.id.ListViewSideBar);
    }

    private void configTools() {
//        musicDBHelper = new MusicDBHelper(getContext());
//        for (int i = 0; i < 20; i++) {
//            musicDBHelper.insert(new MusicData("成都" + i, "赵雷", ""));
//        }
//        musicDataList = musicDBHelper.getQueryAll(MusicData.class);

        adapter = new MusicListAdapter(musicPresenter.getMusicList(), getActivity());
        listView.setAdapter(adapter);
        updaterManager = new UpdaterManager();
        updaterManager.registerUpdaters();
    }

    private class UpdaterManager{
        private IMusicList.MusicListUpdater musicListUpdater = new IMusicList.MusicListUpdater() {
            @Override
            public void updateMusicList(List<MusicData> musicDataList) {
                adapter.updateMusicList(musicDataList);
            }
        };
        private void registerUpdaters() {
            musicPresenter.registMusicListUpdater(musicListUpdater);
        }
        private void unregisterUpdaters() {
            musicPresenter.unregistMusicListUpdater(musicListUpdater);
        }
    }



    private void registerListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicPresenter.playMusic((int) id);
            }
        });
        listViewSideBar.setTextDialog(textDialog);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        musicDBHelper.clear();
        updaterManager.unregisterUpdaters();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        musicDBHelper.clear();
    }
}
