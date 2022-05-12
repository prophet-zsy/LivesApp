package com.example.livesapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.livesMultiProcess.R;
import com.example.livesapp.adapter.CommonAdpater;
import com.example.livesapp.model.beans.StepData;
import com.example.livesapp.model.local.DBHelper;

import java.util.Comparator;
import java.util.List;

public class SportHistoryActivity extends AppCompatActivity {

    private static final String TAG = "SportHistoryActivity";

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.sport_history);
        init();
    }

    private void init() {
        listView = findViewById(R.id.listView);

////        模拟数据，测试使用
//        for (int i = 0; i < 20; i++) {
//            DBHelper.getInstance().insert(new StepData(i, "2021-12-20",  i + 456 +"步"));
//        }

//        查询，装填数据
        List<StepData> dataList = DBHelper.getInstance().getQueryAll(StepData.class);
        Log.d(TAG, "init: " + dataList.toString());
        dataList.sort(new Comparator<StepData>() {
            @Override
            public int compare(StepData o1, StepData o2) {
                return o2.getDate().compareTo(o1.getDate());
            }
        });
        listView.setAdapter(new CommonAdpater<StepData>(dataList, this, R.layout.sport_history_list_item) {
            @Override
            protected void convertView(View view, StepData stepData) {
                TextView date = view.findViewById(R.id.date);
                TextView step = view.findViewById(R.id.step);
                date.setText(stepData.getDate());
                step.setText(stepData.getStep());
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        });
    }


}
