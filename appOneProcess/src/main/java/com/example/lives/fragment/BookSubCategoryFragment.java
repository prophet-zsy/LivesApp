package com.example.lives.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class BookSubCategoryFragment extends Fragment {

    public static BookSubCategoryFragment newInstance() {

        Bundle args = new Bundle();

        BookSubCategoryFragment fragment = new BookSubCategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
