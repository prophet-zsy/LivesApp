package com.example.livesMultiProcess.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.livesMultiProcess.R;
import com.example.livesMultiProcess.activity.SportHistoryActivity;
import com.example.livesMultiProcess.activity.SportSettingActivity;
import com.example.livesMultiProcess.service.StepService;
import com.example.livesMultiProcess.widget.StepArcView;

import static android.content.Context.MODE_PRIVATE;

public class SportFragment extends Fragment {

    private View view;

    private StepArcView stepArc;

    private StepService stepService;
    private boolean isBind = false;
    private ServiceConnection conn;

    public static SportFragment newInstance() {

        Bundle args = new Bundle();

        SportFragment fragment = new SportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sport, null, false);
        initView();
        setupService();
//        testStepArcView();   // 测试圆弧显示效果
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//      返回该界面后，通过动画响应sp的修改
        if (isBind && stepService != null)
            updateArcUi(stepService.getCurStepNum());
    }

    private void initView() {
        stepArc = view.findViewById(R.id.stepArc);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", "onClick");
                Intent intent;
                switch (v.getId()) {
                    case R.id.setting:
                        Log.i("TEST", "setting");
                        intent = new Intent(getActivity(), SportSettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.history:
                        Log.i("TEST", "history");
                        intent = new Intent(getActivity(), SportHistoryActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        };
        Button setting = (Button) view.findViewById(R.id.setting);
        setting.setOnClickListener(onClickListener);
        Button history = (Button) view.findViewById(R.id.history);
        history.setOnClickListener(onClickListener);
    }

    private void setupService() {
        Intent intent = new Intent(getActivity(), StepService.class);
        getActivity().startService(intent);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                stepService = ((StepService.StepBinder) service).getService();
                updateArcUi(stepService.getCurStepNum());
                stepService.initUpdateUi(new UpdateSportUiListener() {
                    @Override
                    public void updateUi(int steps) {
                        updateArcUi(steps);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        isBind = getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void updateArcUi(int steps) {
        String goalStr = getActivity().getSharedPreferences("user", MODE_PRIVATE).getString("stepNumPerDay", "7000");
        int goal = goalStr == null ? -1 : Integer.parseInt(goalStr);
        stepArc.updateUiByAnimation(steps, goal);
    }

    private void shutdownService() {
        if (isBind) {
            getActivity().unbindService(conn);
        }
        getActivity().stopService(new Intent(getActivity(), StepService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdownService();
    }

    private void testStepArcView() {
        final Handler handler = new Handler(Looper.myLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                stepArc.updateUiByAnimation(msg.what, 7000);
                return true;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 7000; i+=1000) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message = new Message();
                    message.what = i;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }
}
