package com.example.livesMultiProcess.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.example.livesMultiProcess.utils.DensityUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.livesMultiProcess.R;
import com.example.livesMultiProcess.adapter.BookCategoryPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class BookCategoryFragment extends Fragment {


    BookCategoryFragment() {

    }

    public static BookCategoryFragment newInstance() {

        Bundle args = new Bundle();

        BookCategoryFragment fragment = new BookCategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TabLayout tabLayout = new TabLayout(getContext());
        ViewPager viewPager = new ViewPager(getContext());
        tabLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(getContext(), 48)));
        tabLayout.setBackgroundColor(getContext().getResources().getColor(R.color.colorAccent));
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        viewPager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewPager.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));

        tabLayout.setId(R.id.tabLayout);
        viewPager.setId(R.id.viewPager);  // 设置adapter时，会需要view id

        linearLayout.addView(tabLayout);
        linearLayout.addView(viewPager);

        // getChildFragmentManager()用于fragment中动态添加fragment,而activity中动态添加fragment则使用getFragmentManager()
        viewPager.setAdapter(new BookCategoryPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        return linearLayout;
    }
}
