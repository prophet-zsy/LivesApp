package com.example.livesapp.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.livesMultiProcess.R;
import com.example.livesapp.utils.DensityUtil;

public class StepArcView extends View {

    Context context;
    Paint paint;

    int stepNum = 0;
    int goal = 7000;
    float progressStepNum = 0.0f;

    private static final int PAINT_STYLE_ARC_BACK = 0x1; // 背景圆弧画笔设置
    private static final int PAINT_STYLE_ARC_PROGRESS = 0x4; // 背景圆弧画笔设置
    private static final int PAINT_STYLE_STEP_TEXT = 0x2; // 步数数字画笔设置
    private static final int PAINT_STYLE_STEPS_TEXT = 0x3; // 步数文字画笔设置

    private static final int stepTextSize = 20; // 步数数字大小  单位sp
    private static final int stepsTextSize = 12; // 步数文字大小 单位sp

    private static final int arcBorderWidth = 20;  // 圆弧宽  单位dp
    private float startAngle = 135;   // 开始弧度
    private float sweepAngle = 270;   // 转过弧度

    private int getStepTextSize() {
        return DensityUtil.sp2px(context, stepTextSize);
    }

    private int getStepsTextSize() {
        return DensityUtil.sp2px(context, stepsTextSize);
    }

    private int getArcBorderWidth() {
        return DensityUtil.dp2px(context, arcBorderWidth);
    }

    private void init(Context contextParam) {
        context = contextParam;
        paint = new Paint();
    }

    private void setPaintStyle(int styleId) {
        paint.setAntiAlias(true); //抗锯齿功能
        switch (styleId) {
            case PAINT_STYLE_ARC_BACK:
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(getArcBorderWidth());
                return;
            case PAINT_STYLE_ARC_PROGRESS:
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(getArcBorderWidth());
                return;
            case PAINT_STYLE_STEP_TEXT:
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(getResources().getColor(R.color.colorAccent, null));
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(DensityUtil.dp2px(context, 1.0f));
                paint.setTextSize(getStepTextSize());
                return;
            case PAINT_STYLE_STEPS_TEXT:
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(getResources().getColor(R.color.colorPrimaryDark, null));
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(DensityUtil.dp2px(context, 1.0f));
                paint.setTextSize(getStepsTextSize());
                return;
        }
    }

    public StepArcView(Context context) {
        super(context);
        init(context);
    }

    public StepArcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public void updateUiByAnimation(int curStepNum, final int curGoal) {
        curStepNum = curStepNum > curGoal ? curGoal : curStepNum;  // 不能大于curGoal
        startAnimation((float) stepNum / (float) goal, (float) curStepNum / (float) curGoal);
        this.stepNum = curStepNum; // 记录stepNum
        this.goal = curGoal;  // 记录goal
    }

    /***
     *  通过动画更新该View
     * @param startValue  0.0f - 1.0f
     * @param endValue 0.0f - 1.0f
     */
    private void startAnimation(float startValue, float endValue) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startValue, endValue);
        valueAnimator.setTarget(this);
        valueAnimator.setDuration(2000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progressStepNum = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    //    一次onDraw对应一个静态界面，而动作则由多次draw来实现
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackArc(canvas);
        drawProgressArc(canvas);
        drawText(canvas);
    }

    private RectF getRectFBySize() {
//        只能在draw阶段使用，因为这时经过测量、布局，宽高已经确定了
        int arcWidth = getWidth();  // 外边界
        RectF rectF = new RectF();
        rectF.left = getArcBorderWidth() / 2;
        rectF.top = getArcBorderWidth() / 2;
        rectF.right = arcWidth - getArcBorderWidth() / 2;
        rectF.bottom = arcWidth - getArcBorderWidth() / 2;
        return rectF;
    }

    private void drawBackArc(Canvas canvas) {
        RectF rectF = getRectFBySize();
        setPaintStyle(PAINT_STYLE_ARC_BACK);
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);
    }

    private void drawProgressArc(Canvas canvas) {
        RectF rectF = getRectFBySize();
        setPaintStyle(PAINT_STYLE_ARC_PROGRESS);
        canvas.drawArc(rectF, startAngle, sweepAngle * progressStepNum, false, paint);
    }

    private void drawText(Canvas canvas) {
//        只能在draw阶段使用，因为这时经过测量、布局，宽高已经确定了
        int arcWidth = getWidth();  // 外边界
        int arcHeight = getHeight();  // 外边界
        float x = arcWidth / 2;
        float y = arcHeight / 2 - getStepTextSize();
        setPaintStyle(PAINT_STYLE_STEP_TEXT);
        canvas.drawText(String.valueOf(stepNum), x, y, paint);
        float xx = arcWidth / 2;
        float yy = arcHeight / 2 + getStepsTextSize();
        setPaintStyle(PAINT_STYLE_STEPS_TEXT);
        canvas.drawText("步数", xx, yy, paint);
    }
}
