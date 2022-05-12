package com.example.livesapp.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.livesMultiProcess.R;
import com.example.livesapp.model.beans.MusicData;
import com.example.livesapp.presenter.Music.IMusicChangeRule;
import com.example.livesapp.presenter.Music.IMusicCommonControl;
import com.example.livesapp.presenter.Music.IMusicPresenter;
import com.example.livesapp.presenter.Music.IMusicProgress;
import com.example.livesapp.presenter.Music.MusicPresenterProxy;
import com.example.livesapp.utils.TimeUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MusicPlayerActivity extends AppCompatActivity {

    private static final String TAG = "MusicPlayerActivity";

    private IMusicPresenter musicPresenter;

    /**
     *     动态变化的view 和他们的设置方法
     */
    public class DynamicInfoViewHolder {
        @BindView(R.id.title_name)
        TextView titleName;
        @BindView(R.id.author_name)
        TextView authorName;

        @BindView(R.id.curTime)
        TextView curTime;
        @BindView(R.id.seekBar)
        SeekBar seekBar;

        @BindView(R.id.totalTime)
        TextView totalTime;

        @BindView(R.id.play)
        ImageView play;
        @BindView(R.id.downloadProgress)
        ProgressBar downloadProgress;

        @BindView(R.id.change_order)
        ImageButton changeOrder;
        @BindView(R.id.like)
        ImageButton like;

        //        playRuleResources 图标数组中各元素编号应和MusicService中PLAY_RULE枚举类型定义的元素编号保持一致
        private IMusicChangeRule.PLAY_RULE[] playRules = new IMusicChangeRule.PLAY_RULE[] {
                IMusicChangeRule.PLAY_RULE.SEQUENTIALLY,
                IMusicChangeRule.PLAY_RULE.SINGLE_LOOP,
                IMusicChangeRule.PLAY_RULE.RANDOM,
        };
        private Map<IMusicChangeRule.PLAY_RULE, Integer> playRuleResources = new HashMap<IMusicChangeRule.PLAY_RULE, Integer>()
        {{
            put(IMusicChangeRule.PLAY_RULE.SEQUENTIALLY, R.mipmap.ic_play_order);
            put(IMusicChangeRule.PLAY_RULE.SINGLE_LOOP, R.mipmap.ic_play_one);
            put(IMusicChangeRule.PLAY_RULE.RANDOM, R.mipmap.ic_play_random);

        }};
        private int playRuleId = 0;
        Unbinder unbinder;

        public DynamicInfoViewHolder() {
            unbinder = ButterKnife.bind(this, MusicPlayerActivity.this);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            unbinder.unbind();
        }

        void setPlayOnClickListener(View.OnClickListener listener) {
            play.setOnClickListener(listener);
        }
        void setSeekBarOnClickListener(SeekBar.OnSeekBarChangeListener listener) {
            seekBar.setOnSeekBarChangeListener(listener);
        }
        void setChangeOrderOnClickListener(View.OnClickListener listener) {
            changeOrder.setOnClickListener(listener);
        }
        void setLikeOnClickListener(View.OnClickListener listener) {
            like.setOnClickListener(listener);
        }
        void setLikeCurMusic(boolean like) {

        }
        void setPlayRule(IMusicChangeRule.PLAY_RULE playRule) {
            for (int i = 0; i < playRules.length; i++) {
                if (playRule == playRules[i]) {
                    this.playRuleId = i;
                    break;
                }
            }
            changeOrder.setBackgroundResource(playRuleResources.get(playRule));
        }
        IMusicChangeRule.PLAY_RULE changePlayOrder() {
            int len = playRules.length;
            playRuleId = (playRuleId + 1) % len;
            IMusicChangeRule.PLAY_RULE playRule = playRules[playRuleId];
            changeOrder.setBackgroundResource(playRuleResources.get(playRule));
            return playRule;
        }
        String getMusicName() {
            return titleName.getText().toString();
        }
        String getAuthorName() {
            return authorName.getText().toString();
        }
        void setMusicAndAuthor(MusicData musicData) {
            titleName.setText(musicData.getName());
            authorName.setText(musicData.getAuthor());
        }
        void setCurDuration(int duration) {
            String dur = TimeUtil.minutesToHHMM(duration);
            totalTime.setText(dur);
            seekBar.setMax(duration);
            seekBar.setMin(0);
        }
        //        先使用setTotalTime获得duration，再使用setCurPosition
        void setCurPosition(int position) {
            String pos = TimeUtil.minutesToHHMM(position);
            curTime.setText(pos);
            seekBar.setProgress(position);
        }
//        播放按钮共下载、播放、暂停三个状态
        void setStatePlay(IMusicCommonControl.PLAY_STATE playState) {
            switch (playState) {
                case PLAYING:
                    downloadProgress.setVisibility(View.GONE);
                    play.setImageResource(R.mipmap.ic_pause);
                    break;
                case PAUSING:
                    downloadProgress.setVisibility(View.GONE);
                    play.setImageResource(R.mipmap.ic_play);
                    break;
                case DOWNLOADING:
                    downloadProgress.setVisibility(View.VISIBLE);
                    play.setImageResource(R.mipmap.ic_play);
                    break;
            }
        }
    }
    DynamicInfoViewHolder dynamicInfoViewHolder;

    @BindView(R.id.return_logo)
    ImageView returnLogo;
    @BindView(R.id.info_logo)
    ImageView infoLogo;
    @BindView(R.id.last_music)
    ImageView lastMusic;
    @BindView(R.id.next_music)
    ImageView nextMusic;

    Unbinder unbinder;
    UpdaterManager updaterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        configTools();
        registerListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updaterManager.registerUpdaters();
        prepareView(); // 重返页面时，更新下信息
    }

    @Override
    protected void onPause() {
        super.onPause();
        updaterManager.unregisterUpdaters();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        dynamicInfoViewHolder = null;
    }

    private void configTools() {
        musicPresenter = MusicPresenterProxy.getProxy(new Class[]{IMusicCommonControl.class, IMusicChangeRule.class, IMusicProgress.class});
        unbinder = ButterKnife.bind(this);
        dynamicInfoViewHolder = new DynamicInfoViewHolder();
        updaterManager = new UpdaterManager();
    }

    private void prepareView() {
        Log.d(TAG, "prepareView: " + musicPresenter.getMusicInfo());
        Log.d(TAG, "prepareView: " + dynamicInfoViewHolder);
        if (musicPresenter.getMusicInfo() != null && dynamicInfoViewHolder != null) {
            dynamicInfoViewHolder.setMusicAndAuthor(musicPresenter.getMusicInfo());
            dynamicInfoViewHolder.setCurDuration(musicPresenter.getCurMusicDuration());
            dynamicInfoViewHolder.setCurPosition(musicPresenter.getCurMusicPosition());
            dynamicInfoViewHolder.setStatePlay(musicPresenter.getPlayState());
            dynamicInfoViewHolder.setPlayRule(musicPresenter.getPlayRule());
        }
    }

    private class UpdaterManager{
        private IMusicCommonControl.MusicInfoUpdater musicInfoUpdater = new IMusicCommonControl.MusicInfoUpdater() {
            @Override
            public void updateMusicInfo(MusicData musicData) {
                dynamicInfoViewHolder.setMusicAndAuthor(musicData);
            }
        };
        private IMusicCommonControl.MusicStateUpdater musicStateUpdater = new IMusicCommonControl.MusicStateUpdater() {
            @Override
            public void updatePlayState(IMusicCommonControl.PLAY_STATE playState) {
                dynamicInfoViewHolder.setStatePlay(playState);
            }
        };
        private IMusicProgress.MusicDurationUpdater musicDurationUpdater = new IMusicProgress.MusicDurationUpdater() {
            @Override
            public void updateCurMusicDuration(int currentDuration) {
                dynamicInfoViewHolder.setCurDuration(currentDuration);
            }
        };
        private IMusicProgress.ProgressUpdater progressUpdater = new IMusicProgress.ProgressUpdater() {
            @Override
            public void updateCurMusicPosition(int currentPosition) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dynamicInfoViewHolder.setCurPosition(currentPosition);
                    }
                });
            }
        };

        private void registerUpdaters() {
            musicPresenter.registMusicInfoUpdater(musicInfoUpdater);
            musicPresenter.registMusicStateUpdater(musicStateUpdater);
            musicPresenter.registDurationUpdater(musicDurationUpdater);
            musicPresenter.registProgressUpdater(progressUpdater);
        }

        private void unregisterUpdaters() {
            musicPresenter.unregistMusicInfoUpdater(musicInfoUpdater);
            musicPresenter.unregistMusicStateUpdater(musicStateUpdater);
            musicPresenter.unregistDurationUpdater(musicDurationUpdater);
            musicPresenter.unregistProgressUpdater(progressUpdater);
        }
    }

    private void registerListener() {
//        注册监听view方的
        returnLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        infoLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MusicPlayerActivity.this)
                        .setTitle(dynamicInfoViewHolder.getMusicName())
                        .setMessage(dynamicInfoViewHolder.getAuthorName())
                        .setPositiveButton("确定", null)
                        .create()
                        .show();
            }
        });
        lastMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPresenter.lastMusic();
            }
        });
        dynamicInfoViewHolder.setPlayOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: setPlayOnClickListener  " + musicPresenter.getPlayState());
                if (musicPresenter.getPlayState() == IMusicCommonControl.PLAY_STATE.PLAYING)
                    musicPresenter.pauseMusic();
                else if (musicPresenter.getPlayState() == IMusicCommonControl.PLAY_STATE.PAUSING)
                    musicPresenter.playMusic();
            }
        });
        nextMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPresenter.nextMusic();
            }
        });
        dynamicInfoViewHolder.setChangeOrderOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                todo 这里 播放规则的改变逻辑应该主要由musicPresenter控制
                musicPresenter.setPlayRule(dynamicInfoViewHolder.changePlayOrder());
            }
        });
        dynamicInfoViewHolder.setLikeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dynamicInfoViewHolder.setSeekBarOnClickListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    dynamicInfoViewHolder.setCurPosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                Log.d(TAG, "onStopTrackingTouch: progress " + progress);
                musicPresenter.seekTo(progress);
            }
        });
    }
}
