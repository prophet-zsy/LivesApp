package com.example.livesapp.presenter.sport;


import android.content.Intent;

import com.example.livesapp.app.MyApp;
import com.example.livesapp.model.beans.StepData;
import com.example.livesapp.model.local.DBHelper;
import com.example.livesapp.receiver.StepEventReceiver;
import com.example.livesapp.service.StepService;
import com.example.livesapp.utils.Step.CountStep;
import com.example.livesapp.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class StepPresenter {

    private static volatile StepPresenter ins;
    private static StepGoalPresenter stepGoalPresenter;

    private CountStep countStep;
    private List<StepNumUpdater> stepNumUpdaters;

    //  当前统计的步数和步数对应时间
    private int curStepNum = 0;
    private String curStepDate;

    private StepEventReceiver stepEventReceiver;

//    定期保存
    private long saveDataTimeInterval = 30 * 1000;  // 单位ms
    private TimeUtil.CycleRun periodicallySave;
    private TimeUtil.CycleRun.WorkTimeUp periodicallyWork;

    public static StepPresenter getInstance() {
        if (ins == null) {
            synchronized (StepPresenter.class) {
                if (ins == null) {
                    ins = new StepPresenter();
                }
            }
        }
        return ins;
    }

    private StepPresenter() {
        stepGoalPresenter = StepGoalPresenter.getInstance();
        stepNumUpdaters = new ArrayList<>();
        initPeriodicallySave();
        initSteps();
        initBroadCast();
        initCountStep();
        startStepService();
    }

    private void initPeriodicallySave() {
        periodicallyWork = new TimeUtil.CycleRun.WorkTimeUp() {
            @Override
            public void doWork() {
                saveStepData();
            }
        };
        periodicallySave = new TimeUtil.CycleRun(saveDataTimeInterval, periodicallyWork);
        periodicallySave.start();
    }

    public void changeSavePeriod(long period) {
        saveDataTimeInterval = period;
        periodicallySave.cancel();
        periodicallySave = new TimeUtil.CycleRun(saveDataTimeInterval, periodicallyWork);
        periodicallySave.start();
    }

    private void initSteps() {
        List<StepData> list = DBHelper.getInstance().getQueryByWhere(StepData.class, "date", new String[]{TimeUtil.getTodayDate()});
        if (list.size() == 1) {
            StepData stepData = list.get(0);
            curStepNum = Integer.parseInt(stepData.getStep().replace("步", ""));
            curStepDate = stepData.getDate();
        } else if (list.size() > 1) {
            throw new RuntimeException("multi rows query from the database");
        } else if (list.size() == 0) {
            curStepNum = 0;
            curStepDate = TimeUtil.getTodayDate();
        }
    }

    private void initCountStep() {
        CountStep.StepNumUpdater updater = new CountStep.StepNumUpdater() {
            @Override
            public void updateStepNum(int steps) {
                curStepNum = steps;
                notifyStepNum();
            }
        };
        countStep = new CountStep(MyApp.getContext(), updater);
        countStep.setStep(curStepNum);  // 计步器取出的步子值开始计步
    }

    private void initBroadCast() {  // 监听广播，触发一些动作（保存、设置、提醒）
        stepEventReceiver = new StepEventReceiver(this);
        MyApp.getContext().registerReceiver(stepEventReceiver, stepEventReceiver.intentFilter);
    }

    private void startStepService() {
        if (StepService.ins == null) {
            Intent intent = new Intent(MyApp.getContext(), StepService.class);
            MyApp.getContext().startService(intent);
        }
    }

    private void notifyStepNum() {
        for (int i = 0; i < stepNumUpdaters.size(); i++) {
            StepNumUpdater stepNumUpdater = stepNumUpdaters.get(i);
            stepNumUpdater.updateStepNum(curStepNum);
        }
    }

    public void saveStepData() {
        DBHelper dbHelper = DBHelper.getInstance();
        List<StepData> list = dbHelper.getQueryByWhere(StepData.class, "date", new String[]{curStepDate});
        if (list.size() == 1) {
            StepData stepData = list.get(0);
            stepData.setStep(curStepNum + "步");
            dbHelper.update(stepData);
        } else if (list.size() == 0) {
            StepData stepData = new StepData();
            stepData.setDate(curStepDate);
            stepData.setStep(curStepNum + "步");
            dbHelper.insert(stepData);
        } else if (list.size() > 1)
            throw new RuntimeException("multi rows query from the database");
    }

    public boolean isNewDay() {
        return TimeUtil.getTimeNow().equals("00:00") && !TimeUtil.getTodayDate().equals(curStepDate);
    }
    public void resetCurStepNum() {
        curStepNum = 0;
    }
    public void refreshDate() {
        curStepDate = TimeUtil.getTodayDate();
    }

    public int getCurStepNum() {
        return curStepNum;
    }

    public void registerStepNumUpdater(StepNumUpdater stepNumUpdater) {
        this.stepNumUpdaters.add(stepNumUpdater);
    }
    public void unregisterStepNumUpdater(StepNumUpdater stepNumUpdater) {
        this.stepNumUpdaters.remove(stepNumUpdater);
    }
    public interface StepNumUpdater {
        void updateStepNum(int steps);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        MyApp.getContext().unregisterReceiver(stepEventReceiver);
    }
}
