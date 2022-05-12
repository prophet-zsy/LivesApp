package com.example.livesMultiProcess.utils.countstep;


import android.content.Context;

/**
 * --对外表现为一个计步器，维护当前步数
 *
 * --为了去除无效运动，增加以下判定：
 * 1.有效步数需要连续10步以上
 * 2.有效步数每两步中间停顿需在3秒以内
 * 例如记录的9步用户停住超过3秒，则前面的记录失效，下次从头开始
 * 连续记录了9步用户还在运动，之前的数据才有效
 */

public class CountStep implements ListenDetectStep {

    private DetectStep detectStep;
    private ListenPassStepNum listenPassStepNum;

    private final int VALID_STEP_NUM = 10;  // 10步以上为有效步数
    private final int STEP_STOP_THRESHOLD = 3000;  // 两步之间超过3秒视为中断

    private long lastStepTime = 0;
    private long curStepTime = 0;

    private int validStepNum = 0;  // 当前步数
    private int stepNum = 1;

    public CountStep(Context context) {
        this.detectStep = new DetectStep(context);
        this.detectStep.setListener(this);
    }

    public void setListenPassStepNum(ListenPassStepNum listener) {
        this.listenPassStepNum = listener;
    }

    private void notifyListener() {
        if (this.listenPassStepNum != null)
            this.listenPassStepNum.stepChanged(validStepNum);
    }

    //    调用该函数代表当前发生了一步
    @Override
    public void countingStep() {
        curStepTime = System.currentTimeMillis();
        if (lastStepTime == 0 || curStepTime - lastStepTime > STEP_STOP_THRESHOLD) {  // 重新开始计步
            stepNum = 1;
        } else stepNum++;
        if (stepNum == VALID_STEP_NUM) {  // 达到10步，这10步计入有效步数
            validStepNum += stepNum;
            notifyListener();
        } else if (stepNum > VALID_STEP_NUM) {  // 10步以上每一步均计入有效步数
            validStepNum++;
            notifyListener();
        }
        lastStepTime = curStepTime;
    }

//    设置从steps开始计步
    public void setStep(int steps) {
        validStepNum = steps;
        stepNum = 0;
        curStepTime = 0;
        lastStepTime = 0;
        notifyListener();
    }
}
