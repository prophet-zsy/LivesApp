package com.example.livesapp.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import com.example.livesapp.utils.DensityUtil;

import com.example.livesMultiProcess.R;

import java.util.ArrayList;
import java.util.List;

public class BottomNavigationBar extends LinearLayout {

    Context context;
    SelectedTabListener selectedTabListener;
    List<BookTab> tabList = new ArrayList<>();

    int defaultWidth;
    int selectedWidth;

    int defaultSelectedIdx = 0;

    public BottomNavigationBar(Context context) {
        super(context);
        initView(context);
    }

    public BottomNavigationBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        setOrientation(HORIZONTAL);
        setBackgroundColor(getResources().getColor(R.color.colorAccent));
        setGravity(Gravity.CENTER);
    }

    public void addTab(String text, int image) {
        BookTab bookTab = new BookTab(getContext());
        bookTab.setText(text);
        bookTab.setImage(image);
        bookTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickEvent((BookTab) v);
            }
        });
        tabList.add(bookTab);
        addView(bookTab, new LayoutParams(0, LayoutParams.MATCH_PARENT, 50));
    }

    private void handleClickEvent(BookTab bookTab) {
        for (int i = 0; i < tabList.size(); i ++) {
            final BookTab tab = tabList.get(i);
            if (tab.equals(bookTab)) {
                if (! tab.isSelected()) {
                    if (selectedTabListener != null) selectedTabListener.onSelectedTab(i);
                    tab.setSelected(true);
                    tab.makeTextVisable();
//                ????????????
                    ValueAnimator valueAnimator = ValueAnimator.ofInt(defaultWidth, selectedWidth);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int width = (int) animation.getAnimatedValue();
                            tab.getLayoutParams().width = width;
                            tab.requestLayout();
                        }
                    });
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(tab.getTextView(), "scaleX", 0, 1);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(tab.getTextView(), "scaleY", 0, 1);
                    ObjectAnimator translationY = ObjectAnimator.ofFloat(tab.getImageView(), "translationY", 0, -DensityUtil.dp2px(context, 6));

                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(valueAnimator, scaleX, scaleY, translationY);
                    animatorSet.setDuration(250);
                    animatorSet.start();
                }
            } else {
                if (tab.isSelected()) {
                    tab.setSelected(false);
                    tab.makeTextNotVisable();
//                  ????????????
                    ValueAnimator valueAnimator = ValueAnimator.ofInt(selectedWidth, defaultWidth);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int width = (int) animation.getAnimatedValue();
                            tab.getLayoutParams().width = width;
                            tab.requestLayout();
                        }
                    });
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(tab.getTextView(), "scaleX", 1, 0);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(tab.getTextView(), "scaleY", 1, 0);
                    ObjectAnimator translationY = ObjectAnimator.ofFloat(tab.getImageView(), "translationY", -DensityUtil.dp2px(context, 6), 0);

                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(valueAnimator, scaleX, scaleY, translationY);
                    animatorSet.setDuration(250);
                    animatorSet.start();
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {  // ??????????????????????????????????????????changed???true????????????????????????????????????
            defaultWidth = (int) (getWidth() / (tabList.size() + 0.5f));  // ????????????????????????n?????????????????????????????????????????????
            selectedWidth = (int) (defaultWidth * 1.5f);  // ??????????????????1.5???????????????
            for (int i = 0; i < tabList.size(); i++) {
                BookTab tab = tabList.get(i);
                ViewGroup.LayoutParams layoutParams = tab.getLayoutParams();
                if (i == defaultSelectedIdx) { // ????????????????????????
                    tab.setSelected(true);
                    tab.makeTextVisable();
                    layoutParams.width = selectedWidth;
                } else {
                    tab.setSelected(false);
                    tab.makeTextNotVisable();
                    layoutParams.width = defaultWidth;
                }
            }
        }
        //        todo ???????????????????????????????????????????????????????????????????????????
    }

    public interface SelectedTabListener {
        public void onSelectedTab(int pos);
    }

    public void setSelectedListener(SelectedTabListener selectedTabListener) {
        this.selectedTabListener = selectedTabListener;
    }

}
