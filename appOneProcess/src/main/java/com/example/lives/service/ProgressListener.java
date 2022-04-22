package com.example.lives.service;

public interface ProgressListener {
    public void updateProgress(int value);
    public void updateCurTime(String string);
    public void updateTotalTime(String string);
}
