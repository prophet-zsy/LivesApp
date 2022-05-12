package com.example.livesMultiProcess.widget;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.livesMultiProcess.utils.DensityUtil;

public class BookTab extends LinearLayout {

    private ImageView imageView;
    private TextView textView;


    public BookTab(Context context) {
        super(context);
        initView(context);
    }

    public BookTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        imageView = new ImageView(context);
        textView = new TextView(context);
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        LayoutParams imageViewLayoutParams = new LayoutParams(DensityUtil.dp2px(context, 24), DensityUtil.dp2px(context, 24));
        addView(imageView, imageViewLayoutParams);
        LayoutParams textViewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(textView, textViewLayoutParams);
    }

    public void setImage(int image) {
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), image));
    }

    public void setText(String text) {
        textView.setText(text);
    }


    public ImageView getImageView() {
        return imageView;
    }

    public TextView getTextView() {
        return textView;
    }

    public void makeTextVisable() {
        textView.setVisibility(VISIBLE);
    }

    public void makeTextNotVisable() {
        textView.setVisibility(GONE);
    }

    public boolean getTextVisable() {
        return textView.getVisibility() == VISIBLE;
    }
}
