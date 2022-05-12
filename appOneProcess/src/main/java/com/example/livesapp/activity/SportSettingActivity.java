package com.example.livesapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.livesMultiProcess.R;
import com.example.livesapp.presenter.sport.StepGoalPresenter;
import com.example.livesapp.utils.TimeUtil;


public class SportSettingActivity extends AppCompatActivity {

    private StepGoalPresenter stepGoalPresenter;

    private EditText stepNumPerDayET;
    private Switch remindSwitch;
    private TextView remindTimeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sport_plan);
        init();
    }

    private void init() {
        stepGoalPresenter = StepGoalPresenter.getInstance();
//      初始化界面控件
        stepNumPerDayET = findViewById(R.id.stepNumPerDay);
        remindSwitch = findViewById(R.id.remind);
        remindTimeTV = findViewById(R.id.remindTime);
//        获取数据
        String stepNumPerDay = String.valueOf(stepGoalPresenter.getGoal());
        boolean remind = stepGoalPresenter.getIfRemind();
        String remindTime = stepGoalPresenter.getRemindTime();
//        装填内容
        stepNumPerDayET.setText(stepNumPerDay);
        remindSwitch.setChecked(remind);
        remindTimeTV.setText(remindTime);
//        设置监听
        remindTimeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = TimeUtil.getHourFromHHMM(remindTimeTV.getText().toString());
                int minute = TimeUtil.getMinuteFromHHMM(remindTimeTV.getText().toString());
                new AlertDialog.Builder(SportSettingActivity.this)
                        .setView(initTimePickerView(hour, minute))
                        .show();
            }
        });
    }

    private View initTimePickerView(int hour, int minute) {
        TimePicker timePicker = new TimePicker(this);
        timePicker.setIs24HourView(true);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                remindTimeTV.setText(TimeUtil.hoursMinutesToHHMM(timePicker.getHour(), timePicker.getMinute()));
            }
        });
        return timePicker;
    }

    public void save(View view) {
//        获取界面控件内容
        String stepNumPerDay = stepNumPerDayET.getText().toString();
        String remind = remindSwitch.isChecked() ? "1" : "0";
        String remindTime = remindTimeTV.getText().toString();
//        保存界面控件内容
        stepGoalPresenter.update(stepNumPerDay, remind, remindTime);
        stepGoalPresenter.save();
//        提示一下 “已保存”
        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
    }
}
