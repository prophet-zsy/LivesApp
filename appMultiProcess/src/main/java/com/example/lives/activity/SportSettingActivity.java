package com.example.lives.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lives.R;


public class SportSettingActivity extends AppCompatActivity {

    private String spFileName = "user";
    private SharedPreferences sp;

    private EditText stepNumPerDayET;
    private Switch remindSwitch;
    private Button remindTimeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sport_plan);
        init();
    }

    private void init() {
//        获取sp中的默认存储
        sp = getSharedPreferences(spFileName, MODE_PRIVATE);
        String stepNumPerDay = sp.getString("stepNumPerDay", "7000");
        String remind = sp.getString("remind", "0");
        String remindTime = sp.getString("remindTime", "20:00");
//      初始化界面控件
        stepNumPerDayET = findViewById(R.id.stepNumPerDay);
        remindSwitch = findViewById(R.id.remind);
        remindTimeButton = findViewById(R.id.remindTime);
//        装填内容
        stepNumPerDayET.setText(stepNumPerDay);
        remindSwitch.setChecked(!"0".equals(remind));
        remindTimeButton.setText(remindTime);
    }

    public void save(View view) {
//        获取界面控件内容
        String stepNumPerDay = stepNumPerDayET.getText().toString();
        String remind = remindSwitch.isChecked() ? "1" : "0";
        String remindTime = remindTimeButton.getText().toString();
//        保存界面控件内容
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("stepNumPerDay", stepNumPerDay);
        editor.putString("remind", remind);
        editor.putString("remindTime", remindTime);
        editor.apply();

//        提示一下 “已保存”
        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
    }
}
