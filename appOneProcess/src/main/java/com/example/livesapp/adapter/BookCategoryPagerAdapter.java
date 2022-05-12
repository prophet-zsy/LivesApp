package com.example.livesapp.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.livesMultiProcess.R;
import com.example.livesapp.app.MyApp;
import com.example.livesapp.fragment.BookSubCategoryFragment;

public class BookCategoryPagerAdapter extends FragmentPagerAdapter {

    String[] tabTitle;

    public BookCategoryPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        tabTitle = MyApp.getContext().getResources().getStringArray(R.array.book_category_tab);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        return BookSubCategoryFragment.newInstance();
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
