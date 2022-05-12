package com.example.livesapp.presenter.sport;

import com.example.livesapp.model.local.SharedPreferenceManager;


public class StepGoalPresenter {

    private static volatile StepGoalPresenter ins;

    private static final String goalToken = "stepNumPerDay";
    private static final String ifRemindToken = "remind";
    private static final String remindTimeToken = "remindTime";

    private int goal = -1;
    private Boolean ifRemind = null;
    private String remindTime = null;

    public static StepGoalPresenter getInstance() {
        if (ins == null) {
            synchronized (StepGoalPresenter.class) {
                if (ins == null) {
                    ins = new StepGoalPresenter();
                }
            }
        }
        return ins;
    }

    public int getGoal() {
        if (goal == -1) {
            String goalStr = SharedPreferenceManager.get(goalToken, "7000");
            goal = Integer.parseInt(goalStr);
        }
        return goal;
    }

    public boolean getIfRemind() {
        if (ifRemind == null) {
            String ifRemindStr = SharedPreferenceManager.get(ifRemindToken, "0");
            ifRemind = "1".equals(ifRemindStr);
        }
        return ifRemind;
    }

    public String getRemindTime() {
        if (remindTime == null) {
            remindTime = SharedPreferenceManager.get(remindTimeToken, "20:00");
        }
        return remindTime;
    }

    public void update(String goal, String ifRemind, String remindTime) {
        this.goal = Integer.parseInt(goal);
        this.ifRemind = "1".equals(ifRemind);
        this.remindTime = remindTime;
    }

    public boolean save() {
        SharedPreferenceManager.storeAsync(goalToken, String.valueOf(goal));
        SharedPreferenceManager.storeAsync(ifRemindToken, String.valueOf(ifRemind ? 1 : 0));
        SharedPreferenceManager.storeAsync(remindTimeToken, remindTime);
        return true;
    }
}
