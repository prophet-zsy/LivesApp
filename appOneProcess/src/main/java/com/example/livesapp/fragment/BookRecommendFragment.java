package com.example.livesapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class BookRecommendFragment extends Fragment {


    BookRecommendFragment() {

    }

    public static BookRecommendFragment newInstance() {

        Bundle args = new Bundle();

        BookRecommendFragment fragment = new BookRecommendFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
