package com.example.lives.utils.countstep;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


/**
 * 检测是否是步点
 * 先识别波峰波谷，然后根据波峰波谷时间和振幅信息判定是否为步点
 */

public class DetectStep implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;

    private ListenDetectStep listenDetectStep;

    //   上次传感器的值
    private float gravityOld = 0;
    //   当前是否上升
    private boolean isDirectionUp = false;
    //   上一点的状态，上升还是下降
    private boolean lastStatus = false;
    //   持续上升次数
    private int continueUpCount = 0;
    //   上一点的持续上升的次数，为了记录波峰的上升次数
    int continueUpFormerCount = 0;
    //波峰值
    private float peakOfWave = 0;
    //波谷值
    private float valleyOfWave = 0;
    //此次波峰的时间
    private long timeOfThisPeak = 0;
    //上次波峰的时间
    private long timeOfLastPeak = 0;
    //当前的时间
    long timeOfNow = 0;
    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    final float InitialValue = (float) 1.3;
    //初始阈值
    float ThresholdValue = (float) 2.0;
    //波峰波谷时间差
    int TimeInterval = 250;

    final int ValueNum = 4;
    //用于存放计算阈值的波峰波谷差值
    float[] tempValue = new float[ValueNum];
    int tempCount = 0;


    public DetectStep(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void setListener(ListenDetectStep listener) {
        listenDetectStep = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        取出三轴方向的数据，计算并检测
        float xVal = event.values[0];
        float yVal = event.values[1];
        float zVal = event.values[2];

        float gravityNew = (float) Math.sqrt(xVal * xVal + yVal * yVal + zVal * zVal);
        detectorNewStep(gravityNew);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void detectorNewStep(float gravityNew) {
        if (gravityOld != 0) {  // 如果存在旧值，则尝试检测波峰
            if (detectorPeak(gravityNew, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();
//                如果波峰时间间隔符合要求 且 波峰波谷差值大于阈值，则视为有效的一步
                if (timeOfNow - timeOfLastPeak >= TimeInterval && (peakOfWave - valleyOfWave >= ThresholdValue)) {
                    timeOfThisPeak = timeOfNow;
//                 这里视为有效的一步，通知计步类CountStep进行计步
                    listenDetectStep.countingStep();
                }
//                如果波峰时间间隔符合要求 且 波峰波谷差值符合要求，则更新检测步子的阈值
                if (timeOfNow - timeOfLastPeak >= TimeInterval && (peakOfWave - valleyOfWave >= InitialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThresholdValue = peakValleyThreshold(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = gravityNew;
    }

    private float peakValleyThreshold(float diff) {
        float tempThreshold = ThresholdValue;
        if (tempCount < ValueNum) {  // 记录数量不够，继续记录
            tempValue[tempCount] = diff;
            tempCount++;
        } else { // 数据足够，移除最早的，再加入一个
            tempThreshold = averageDiff(tempValue, ValueNum);
            for (int i = 1; i < ValueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[ValueNum - 1] = diff;
        }
        return tempThreshold;
    }

    private float averageDiff(float[] tempValue, int valueNum) {
//        计算均值
        float ave = 0;
        for (int i = 0; i < valueNum; i++) {
            ave += tempValue[i];
        }
        ave = ave / ValueNum;
//        对均值进行梯度化，只使用梯度值作为阈值（可能这样稳定点吧？）
        if (ave >= 8)
            ave = (float) 4.3;
        else if (ave >= 7 && ave < 8)
            ave = (float) 3.3;
        else if (ave >= 4 && ave < 7)
            ave = (float) 2.3;
        else if (ave >= 3 && ave < 4)
            ave = (float) 2.0;
        else {
            ave = (float) 1.3;
        }
        return ave;
    }

    private boolean detectorPeak(float gravityNew, float gravityOld) {
        lastStatus = isDirectionUp;
        if (gravityNew >= gravityOld) { // 上升
            isDirectionUp = true;
            continueUpCount++;
        } else { // 下降
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

        if (!isDirectionUp && lastStatus && (continueUpFormerCount >= 2 || gravityOld > 20)) {
//            如果连续上升两次或波峰值大于20，记录波峰值
            peakOfWave = gravityOld;
            return true;
        } else if (!lastStatus && isDirectionUp) {
//            记录波谷值
            valleyOfWave = gravityOld;
            return false;
        } else return false;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        sensorManager.unregisterListener(this);
        sensorManager = null;
        sensor = null;
    }
}
