package com.example.livesMultiProcess.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ListViewSideBar extends LinearLayout {

    private String[] idxs = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};

    private TextView textDialog;

    public ListViewSideBar(Context context) {
        super(context);
        initView();
    }

    public ListViewSideBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void initView() {
        setOrientation(VERTICAL);
        for (int i = 0; i < idxs.length; i++) {
            TextView textView = new TextView(getContext());
            textView.setText(idxs[i]);
            addView(textView);
        }
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    public void setTextDialog(TextView textDialog) {
        this.textDialog = textDialog;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.i("ACTION_UP", "");
                textDialog.setVisibility(GONE);
                break;
            case MotionEvent.ACTION_DOWN:
                float y = event.getY();
                Log.i("ACTION_DOWN", String.valueOf(y));
                int chooseId = (int) (y / getHeight() * idxs.length);  // 点击位置的纵坐标所在比例 对应数组位置即 选中元素
                String dialogText = idxs[chooseId];

                textDialog.setText(dialogText);
                textDialog.setVisibility(VISIBLE);
                break;
            case MotionEvent.ACTION_MOVE:
                y = event.getY();
                Log.i("ACTION_MOVE", String.valueOf(y));
                chooseId = (int) (y / getHeight() * idxs.length);  // 点击位置的纵坐标所在比例 对应数组位置即 选中元素
                dialogText = idxs[chooseId];

                textDialog.setText(dialogText);
                break;
        }
        return true;
//        todo 返回false，代表事件未被消费，返回true，代表事件在此环节已经被完全消费
//        todo 三者调用顺序 onTouch—>onTouchEvent—>onclick ， onTouch和onclick需要实现对应的接口，onTouchEvent是view的成员方法
//        todo 这里默认返false，事件还要传递到onclick那里，因为只需要监听ACTION_DOWN， 所以需要返回true，来消费掉事件，以便监听不抬起的后续事件
//        return super.onTouchEvent(event);
    }
}
