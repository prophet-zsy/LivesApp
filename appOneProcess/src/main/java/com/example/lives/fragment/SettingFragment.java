package com.example.lives.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {

    public static SettingFragment newInstance() {

        Bundle args = new Bundle();

        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
