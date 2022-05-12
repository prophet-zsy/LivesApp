package com.example.livesapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.livesapp.presenter.sport.StepGoalPresenter;
import com.example.livesapp.presenter.sport.StepPresenter;
import com.example.livesapp.service.StepService;
import com.example.livesapp.utils.TimeUtil;

public class StepEventReceiver extends BroadcastReceiver {

    private StepPresenter stepPresenter;
    private StepGoalPresenter stepGoalPresenter;
    public IntentFilter intentFilter;

    public StepEventReceiver(StepPresenter stepPresenter) {
        this.stepPresenter = stepPresenter;
        stepGoalPresenter = StepGoalPresenter.getInstance();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_SCREEN_ON:
                stepPresenter.changeSavePeriod(30 * 1000);
                break;
            case Intent.ACTION_SCREEN_OFF:
                stepPresenter.changeSavePeriod(60 * 1000);
                break;
            case Intent.ACTION_SHUTDOWN:
                stepPresenter.saveStepData();
                break;
            case Intent.ACTION_DATE_CHANGED: // 日期自动或手动发生变化，会发出这个广播，只有日期增大时才会广播，变成过去的日期不会发生广播
            case Intent.ACTION_TIME_CHANGED: // 手动修改时间发出这个广播
                stepPresenter.saveStepData();
                if (stepPresenter.isNewDay()) {  // 新的一天，重置计数
                    stepPresenter.resetCurStepNum();
                    stepPresenter.refreshDate();
                }
                break;
            case Intent.ACTION_TIME_TICK: // 每分钟发出这个广播
                if (stepGoalPresenter.getIfRemind()) {
                    if (stepGoalPresenter.getRemindTime().equals(TimeUtil.getTimeNow())) {
                        StepService.ins.remindNotification();
                    }
                }
                break;
        }
    }
}
