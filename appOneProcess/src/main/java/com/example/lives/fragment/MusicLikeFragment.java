package com.example.lives.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lives.beans.MusicData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicLikeFragment extends MusicBaseFragment {
    private static final String TAG = "MusicLikeFragment";
    private SharedPreferences sharedPreferences;
    private List<MusicData> musicLikeList;

    public static MusicLikeFragment newInstance() {
        Log.i(TAG, "newInstance");
        Bundle args = new Bundle();
        MusicLikeFragment fragment = new MusicLikeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        init();
    }

    private void init() {
//        todo pagerAdapter 旁边的fragment会进行预加载，因此相邻fragment之间的数据要么统一初始化，要么相互独立
        Set<String> set = getMusicLikeNamesFromSP();
        musicLikeList = new ArrayList<>();
        for (int i = 0; i < musicDataList.size(); i++) {
            if (set.contains(musicDataList.get(i).getPath()))
                musicLikeList.add(musicDataList.get(i));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private Set<String> stringToSet(String musicLikeNames) {
        String[] split = musicLikeNames.split(",");
        Set<String> set = new HashSet<>();
        for (int i = 0; i < split.length; i++)
            set.add(split[i]);
        return set;
    }

    private String setToString(Set<String> set) {
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : set) {
            stringBuffer.append(str);
            stringBuffer.append(",");
        }
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }

    private Set<String> getMusicLikeNamesFromSP() {
        sharedPreferences = getContext().getSharedPreferences("musicLike", Context.MODE_PRIVATE);
        String musicLikeNames = sharedPreferences.getString("musicLike", "");
        return stringToSet(musicLikeNames);
    }

    private void storeMusicLikeNamesIntoSP(Set<String> set) {
        String musicLikeNames = setToString(set);
        sharedPreferences = getContext().getSharedPreferences("musicLike", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("musicLike", musicLikeNames);
        edit.apply();
    }
}
