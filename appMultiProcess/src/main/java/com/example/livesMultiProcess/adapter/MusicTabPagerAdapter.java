package com.example.livesMultiProcess.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.livesMultiProcess.R;
import com.example.livesMultiProcess.app.MyApp;
import com.example.livesMultiProcess.fragment.MusicLikeFragment;
import com.example.livesMultiProcess.fragment.MusicListFragment;

public class MusicTabPagerAdapter extends FragmentPagerAdapter {

    String[] tabTitle;

    public MusicTabPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        tabTitle = MyApp.getContext().getResources().getStringArray(R.array.music_tab);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = MusicListFragment.newInstance();
                break;
            case 1:
                fragment = MusicLikeFragment.newInstance();
                break;
        }
        return fragment;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return tabTitle.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitle[position];
    }
}
