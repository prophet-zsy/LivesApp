package com.example.livesMultiProcess.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.example.livesMultiProcess.R;
import com.example.livesMultiProcess.adapter.MusicTabPagerAdapter;
import com.example.livesMultiProcess.utils.DensityUtil;
import com.google.android.material.tabs.TabLayout;

public class MusicContainerFragment extends MusicBaseFragment {

    private static final String TAG = "MusicContainerFragment";
    private Context context;

    public static MusicContainerFragment newInstance(Context context) {
        Log.i(TAG, "newInstance");
        MusicContainerFragment fragment = new MusicContainerFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initViewPager();
    }

    private View initViewPager() {
        Log.i(TAG, "initViewPager");
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TabLayout tabLayout = new TabLayout(getContext());
        ViewPager viewPager = new ViewPager(getContext());
        tabLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(getContext(), 48)));
        tabLayout.setBackgroundColor(getContext().getResources().getColor(R.color.colorAccent));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        viewPager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewPager.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));

        tabLayout.setId(R.id.tabLayout);
        viewPager.setId(R.id.viewPager);  // 设置adapter时，会需要view id

        linearLayout.addView(tabLayout);
        linearLayout.addView(viewPager);

        // getChildFragmentManager()用于fragment中动态添加fragment,而activity中动态添加fragment则使用getFragmentManager()
        viewPager.setAdapter(new MusicTabPagerAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
        return linearLayout;
    }
}
