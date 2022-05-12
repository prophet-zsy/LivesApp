package com.example.livesapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.livesapp.activity.SportHistoryActivity;
import com.example.livesapp.activity.SportSettingActivity;
import com.example.livesapp.presenter.sport.StepGoalPresenter;
import com.example.livesapp.presenter.sport.StepPresenter;
import com.example.livesapp.widget.StepArcView;


public class SportFragment extends Fragment {
    private static final String TAG = "SportFragment";
    private StepPresenter stepPresenter;
    private StepGoalPresenter stepGoalPresenter;
    private UpdaterManager updaterManager;

    private View view;
    private StepArcView stepArc;
    private Button setting;
    private Button history;

    public static SportFragment newInstance() {
        SportFragment fragment = new SportFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        stepPresenter = StepPresenter.getInstance();
        stepGoalPresenter = StepGoalPresenter.getInstance();
        updaterManager = new UpdaterManager();
        updaterManager.registerUpdater();
        view = inflater.inflate(R.layout.sport, null, false);
        initView();
//        testStepArcView();   // 测试圆弧显示效果
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//      返回该界面后，通过动画响应sp的修改
        updateArcUi(stepPresenter.getCurStepNum(), stepGoalPresenter.getGoal());
    }

    private void initView() {
        stepArc = view.findViewById(R.id.stepArc);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                switch (v.getId()) {
                    case R.id.setting:
                        intent = new Intent(getActivity(), SportSettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.history:
                        Log.d(TAG, "onClick: ");
                        intent = new Intent(getActivity(), SportHistoryActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        };
        setting = view.findViewById(R.id.setting);
        setting.setOnClickListener(onClickListener);
        history = view.findViewById(R.id.history);
        history.setOnClickListener(onClickListener);
    }

    private void updateArcUi(int steps, int goal) {
        stepArc.updateUiByAnimation(steps, goal);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updaterManager.unregisterUpdater();
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

    private class UpdaterManager{
        StepPresenter.StepNumUpdater stepNumUpdater = new StepPresenter.StepNumUpdater() {
            @Override
            public void updateStepNum(int steps) {
                updateArcUi(steps, stepGoalPresenter.getGoal());
            }
        };

        private void registerUpdater() {
            stepPresenter.registerStepNumUpdater(stepNumUpdater);
        }
        private void unregisterUpdater() {
            stepPresenter.unregisterStepNumUpdater(stepNumUpdater);
        }
    }

}
