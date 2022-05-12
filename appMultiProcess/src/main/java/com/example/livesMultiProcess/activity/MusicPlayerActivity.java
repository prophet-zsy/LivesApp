package com.example.livesMultiProcess.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.livesMultiProcess.R;
import com.example.livesMultiProcess.app.MyApp;
import com.example.livesMultiProcess.beans.MusicData;
import com.example.livesMultiProcess.service.IMusicBinder;
import com.example.livesMultiProcess.utils.TimeUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MusicPlayerActivity extends AppCompatActivity {

    private static final String TAG = "MusicPlayerActivity";

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

        @BindView(R.id.change_order)
        ImageButton changeOrder;
        @BindView(R.id.like)
        ImageButton like;

        //        playRuleResources 图标数组中各元素编号应和MusicService中PLAY_RULE枚举类型定义的元素编号保持一致
        private int[] playRuleResources = {R.mipmap.ic_play_order, R.mipmap.ic_play_one, R.mipmap.ic_play_random};
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
        void setPlayRule(int playRuleId) {
            this.playRuleId = playRuleId;
            changeOrder.setBackgroundResource(playRuleResources[playRuleId]);
        }
        int changePlayOrder() {
            int len = playRuleResources.length;
            playRuleId = (playRuleId + 1) % len;
            changeOrder.setBackgroundResource(playRuleResources[playRuleId]);
            return playRuleId;
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
        void setStatePlay(boolean isPlaying) {
            if (isPlaying) play.setImageResource(R.mipmap.ic_pause);
            else play.setImageResource(R.mipmap.ic_play);
        }
    }
    DynamicInfoViewHolder dynamicInfoViewHolder;

    private class ProcessUpdateThread extends Thread {
        private boolean running = true;
        public void stopSelf() {
            running = false;
        }
        @Override
        public void run() {
            while (running) {
                // 500 ms 更新一次
                updateProgress();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        private void updateProgress() {
            if (checkMusicBinder()) {
                try {
                    int curMusicPosition = musicBinder.getCurMusicPosition();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dynamicInfoViewHolder.setCurPosition(curMusicPosition);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    ProcessUpdateThread thread;

    @BindView(R.id.return_logo)
    ImageView returnLogo;
    @BindView(R.id.info_logo)
    ImageView infoLogo;
    @BindView(R.id.last_music)
    ImageView lastMusic;
    @BindView(R.id.next_music)
    ImageView nextMusic;

    Unbinder unbinder;

    IMusicBinder musicBinder;
    List<MusicData> musicDataList;
    int curMusicId = -1;
    int curPosition = -1;
    int curDuration = -1;
    volatile boolean isPlaying = false;
    int playRuleId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        configTools();
        getMusicBinder();
        prepareView();
        registerListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        dynamicInfoViewHolder = null;
        thread.stopSelf();
        thread = null;
    }

    private void configTools() {
        unbinder = ButterKnife.bind(this);
        dynamicInfoViewHolder = new DynamicInfoViewHolder();
        thread = new ProcessUpdateThread();
        thread.start();
    }

    private void getMusicBinder() {
        //        通过异步调用+回调，获取全局MusicBinder
        MyApp.MusicServiceProxy.addListener(new MyApp.MusicServiceProxy.GetMusicServiceListener() {
            @Override
            public void workAfterMusicBinderReady(IMusicBinder musicBinder, List<MusicData> musicDataList) {
                MusicPlayerActivity.this.musicBinder = musicBinder;
                MusicPlayerActivity.this.musicDataList = musicDataList;
                try {
                    curMusicId = musicBinder.getCurMusicId();
                    isPlaying = musicBinder.isPlaying();
                    curPosition = musicBinder.getCurMusicPosition();
                    curDuration = musicBinder.getCurMusicDuration();
                    playRuleId = musicBinder.getPlayRule();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prepareView();
                    }
                });
            }
        });
        MyApp.MusicServiceProxy.getMusicBinderAsync();
    }

    private boolean checkMusicBinder() {
        if (musicBinder == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MusicPlayerActivity.this, "失去与MusicService的连接", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        } else return true;
    }

    private void prepareView() {
        if (curMusicId != -1 && checkMusicBinder() && dynamicInfoViewHolder != null) {
            dynamicInfoViewHolder.setMusicAndAuthor(musicDataList.get(curMusicId));
            dynamicInfoViewHolder.setCurDuration(curDuration);
            dynamicInfoViewHolder.setCurPosition(curPosition);
            dynamicInfoViewHolder.setStatePlay(isPlaying);
            dynamicInfoViewHolder.setPlayRule(playRuleId);
        }
    }

    private void registerListener() {
//        注册监听view方的
        returnLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // 销毁前 携带信息回去
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
                if (checkMusicBinder()) {
                    try {
                        curMusicId = musicBinder.lastMusic();
                        curDuration = musicBinder.getCurMusicDuration();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dynamicInfoViewHolder.setMusicAndAuthor(musicDataList.get(curMusicId));
                    dynamicInfoViewHolder.setCurDuration(curDuration);
                    dynamicInfoViewHolder.setStatePlay(isPlaying = true);
                }
            }
        });
        dynamicInfoViewHolder.setPlayOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMusicBinder() && curMusicId != -1) {  // 如果连接了MusicService且选中了音乐
                    try {
                        curDuration = musicBinder.getCurMusicDuration();
                        if (musicBinder.isPlaying()) {
                            dynamicInfoViewHolder.setStatePlay(isPlaying = false);
                            musicBinder.pauseMusic();
                        } else {
                            dynamicInfoViewHolder.setStatePlay(isPlaying = true);
                            musicBinder.playMusic(curMusicId);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dynamicInfoViewHolder.setMusicAndAuthor(musicDataList.get(curMusicId));  // 再更新下当前歌曲信息
                    dynamicInfoViewHolder.setCurDuration(curDuration);  // 再更新下当前歌曲时长
                }
            }
        });
        nextMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMusicBinder()) {
                    try {
                        curMusicId = musicBinder.nextMusic();
                        curDuration = musicBinder.getCurMusicDuration();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dynamicInfoViewHolder.setMusicAndAuthor(musicDataList.get(curMusicId));
                    dynamicInfoViewHolder.setCurDuration(curDuration);
                    dynamicInfoViewHolder.setStatePlay(isPlaying = true);
                }
            }
        });
        dynamicInfoViewHolder.setChangeOrderOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMusicBinder()) {
                    int orderRule = dynamicInfoViewHolder.changePlayOrder();
                    try {
                        musicBinder.setPlayRule(orderRule);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
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

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (checkMusicBinder()) {
                    int progress = seekBar.getProgress();
                    try {
                        musicBinder.seekTo(progress);
                        curPosition = musicBinder.getCurMusicPosition();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dynamicInfoViewHolder.setCurPosition(curPosition);
                }
            }
        });
    }
}
