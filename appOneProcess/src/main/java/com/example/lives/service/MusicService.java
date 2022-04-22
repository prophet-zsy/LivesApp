package com.example.lives.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.lives.app.MyApp;
import com.example.lives.beans.MusicData;
import com.example.lives.network.music.DownloadMusicNetProxy;
import com.example.lives.network.music.GetMusicListProxy;
import com.example.lives.utils.MediaPlayerManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 持有MediaPlayerManager，向外提供音乐播放服务
 */
public class MusicService extends Service {
    private static final String TAG = "MusicService";

    MediaPlayerManager mediaPlayerManager;

    List<Integer> playHistory = new LinkedList<>();  // 切换下一首，则将上一首放入历史

    PLAY_RULE playRule = PLAY_RULE.SEQUENTIALLY;
    Random random = new Random();
    //    所有歌曲信息
    List<MusicData> musicDataList;
    //    当前歌曲信息
    int curMusicId = -1;  // 播放的是哪个音乐 和 mediaPlayerManager 装载的音乐文件保持一致
    long duration;

    MusicBinder musicBinder;    // 作为服务端向外提供服务
    IMusicView musicViewBinder;  // 作为客户端代理和view中的service进行通信


    private void init(Intent intent) {
        GetMusicListProxy.getMusicList(new GetMusicListProxy.MusicListListener() {  // 异步点
            @Override
            public void setMusicDataList(List<MusicData> musicDataList) {
                MusicService.this.musicDataList = musicDataList;
                musicBinder.notifyMusicDataListReady();
            }
        });
        MyApp.MusicViewProxy.getMusicViewBinderAsync(new MyApp.MusicViewProxy.GetMusicViewServiceListener() {  // 异步点
            @Override
            public void workAfterMusicViewBinderReady(IMusicView musicViewBinder) {
                MusicService.this.musicViewBinder = musicViewBinder;
            }
        });
        initMediaPlayerManager();
    }

    private boolean checkMusicViewBinder() {
        if (musicViewBinder == null) {
            Toast.makeText(this, "与musicViewBinder的连接丢失", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    private void initMediaPlayerManager() {
        mediaPlayerManager = MediaPlayerManager.getMediaPlayerManager();
        mediaPlayerManager.setOnPlayCompleteListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                MusicService主动下一首
                if (checkMusicViewBinder()) {
                    next();
                    try {
                        musicViewBinder.setCurMusicUI(curMusicId);  // 通过Binder 远程设置UI的信息
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 判断是否在播放
     * @return
     */
    private boolean isPlaying() {
        return mediaPlayerManager.isPlaying();
    }
    /**
     * 准备并播放
     * 分需要下载（异步）和不需要下载两种情况
     */
    private void prepareAndStart() {
        String storePath = getFilesDir().getPath();
        String musicName = musicDataList.get(curMusicId).getPath();
        if (!DownloadMusicNetProxy.isDownloaded(storePath, musicName)) {  // 异步点  如果需要下载
            DownloadMusicNetProxy.downloadMusic(storePath, musicName, new DownloadMusicNetProxy.MusicDownloadListener(){
                @Override
                public void workAfterDownload() {
                    mediaPlayerManager.restart(storePath + "/" + musicName);
                }
            });
        } else {  // 不需要下载
            mediaPlayerManager.restart(storePath + "/" + musicName);
        }
    }

    /**
     * 播放
     */
    private void start() {
        Log.d(TAG, "start: ");
        mediaPlayerManager.start();
    }
    /**
     * 暂停
     */
    private void pause() {
        mediaPlayerManager.pause();
    }

    /**
     * 上一首
     */
    private int last() {
        Log.d(TAG, "last: " + playHistory);
        if(playHistory.size() == 0) return curMusicId;
        curMusicId = playHistory.get(playHistory.size()-1);
        playHistory.remove(playHistory.size()-1);
        prepareAndStart();
        return curMusicId;
    }
    /**
     * 下一首
     */
    private int next() {
        switch (playRule) {
            case SEQUENTIALLY:
                sequentNextMusic();
                break;
            case SINGLE_LOOP:
                singleLoopNextMusic();
                break;
            case RANDOM:
                randomNextMusic();
                break;
        }
        return curMusicId;
    }
    /**
     * 顺序播放下一首
     */
    private void sequentNextMusic() {
        if (curMusicId == musicDataList.size() - 1) {  // 最后一首了
            return;
        } else {
            if (curMusicId != -1) playHistory.add(curMusicId);
            curMusicId ++;
            prepareAndStart();
        }
    }
    /**
     * 单曲循环播放下一首
     */
    private void singleLoopNextMusic() {
        String storePath = getFilesDir().getPath();
        String musicName = musicDataList.get(curMusicId).getPath();
        mediaPlayerManager.restart(storePath + "/" + musicName);
    }
    /**
     * 随机播放下一首
     */
    private void randomNextMusic() {
        if (curMusicId != -1) playHistory.add(curMusicId);
        int nextMusicId;
        do {
            nextMusicId = random.nextInt(musicDataList.size());  // 不要和上一首歌曲一样
        } while (nextMusicId == curMusicId);
        curMusicId = nextMusicId;
        prepareAndStart();
    }

    /**
     * 定位到position的位置
     * @param position  单位 s
     */
    private void seek(int position) {
        mediaPlayerManager.seekTo(position * 1000);
    }

    /**
     * 设置播放规则
     * @param playRule
     */
    private void setPlayRule(PLAY_RULE playRule) {
        this.playRule = playRule;
    }

    /**
     * 获取播放规则
     * @return
     */
    private PLAY_RULE getPlayRule() {
        return this.playRule;
    }
    /**
     * 获取当前播放位置 单位 s
     * @return
     */
    private int getCurMusicPosition() {
        if (mediaPlayerManager.getCurrentPosition() == -1) return -1;
        return (int) (mediaPlayerManager.getCurrentPosition() / 1000);
    }

    /**
     * 获取当前歌曲总时长 单位 s
     * @return
     */
    private int getCurMusicDuration() {
        if (mediaPlayerManager.getMediaDuration() == -1) return -1;
        return (int) (mediaPlayerManager.getMediaDuration() / 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public enum PLAY_RULE {
        SEQUENTIALLY(0), SINGLE_LOOP(1), RANDOM(2);
        int value;
        PLAY_RULE(int val) {
            this.value = val;
        }
        static PLAY_RULE parseInt(int val) {
            switch (val) {
                case 0:
                    return SEQUENTIALLY;
                case 1:
                    return SINGLE_LOOP;
                case 2:
                    return RANDOM;
            }
            return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        init(intent);
        musicBinder = new MusicBinder();
        return musicBinder;
    }

    public class MusicBinder extends IMusicBinder.Stub {
        private static final String TAG = "MusicBinder";
        private Lock lock = new ReentrantLock();
        private Condition musicDataListNotReady = lock.newCondition();
        public void notifyMusicDataListReady () {
            Log.i(TAG, "notifyMusicDataListReadySTART");
            lock.lock();
            musicDataListNotReady.signal();
            lock.unlock();
            Log.i(TAG, "notifyMusicDataListReadyFINISH");
        }
//        这里先同步处理（等musicDataList就绪后再返回）吧，异步设置监听通过aidl有点麻烦
        @Override
        public List<MusicData> getMusicList() throws RemoteException {
            Log.i(TAG, "getMusicList  " + Thread.currentThread().getName());
            while (musicDataList == null) {
                lock.lock();
                try {
                    musicDataListNotReady.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
            Log.d(TAG, "getMusicList() returned: " + musicDataList);
            return musicDataList;
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            boolean playing = MusicService.this.isPlaying();
            Log.d(TAG, "isPlaying() returned: " + playing);
            return playing;
        }

        @Override
        public boolean playMusic(int musicId) throws RemoteException {
            Log.d(TAG, "playMusic() called with: musicId = [" + musicId + "]");
            if (curMusicId != musicId) {
                curMusicId = musicId;
                prepareAndStart();
            } else {
                if (!isPlaying())
                    start();
            }
            return true;
        }

        @Override
        public boolean pauseMusic() throws RemoteException {
            Log.d(TAG, "pauseMusic: ");
            pause();
            return true;
        }

        @Override
        public int nextMusic() throws RemoteException {
            int next = next();
            Log.d(TAG, "nextMusic() returned: " + next);
            return next;
        }

        @Override
        public int lastMusic() throws RemoteException {
            int last = last();
            Log.d(TAG, "lastMusic() returned: " + last);
            return last;
        }

        @Override
        public boolean seekTo(int position) throws RemoteException {
            Log.d(TAG, "seekTo() called with: position = [" + position + "]");
            seek(position);
            return true;
        }

        @Override
        public int getCurMusicId() throws RemoteException {
            Log.d(TAG, "getCurMusicId() returned: " + curMusicId);
            return curMusicId;
        }

        @Override
        public int getCurMusicPosition() throws RemoteException {
            int curMusicPosition = MusicService.this.getCurMusicPosition();
            Log.d(TAG, "getCurMusicPosition() returned: " + curMusicPosition);
            return curMusicPosition;
        }

        @Override
        public int getCurMusicDuration() throws RemoteException {
            int curMusicDuration = MusicService.this.getCurMusicDuration();
            Log.d(TAG, "getCurMusicDuration() returned: " + curMusicDuration);
            return curMusicDuration;
        }

        @Override
        public int getPlayRule() throws RemoteException {
            PLAY_RULE playRule = MusicService.this.getPlayRule();
            Log.d(TAG, "getPlayRule() returned: " + playRule.value);
            return playRule.value;
        }

        @Override
        public boolean setPlayRule(int playRule) throws RemoteException {
            Log.d(TAG, "setPlayRule() called with: playRule = [" + playRule + "]");
            MusicService.this.setPlayRule(PLAY_RULE.parseInt(playRule));
            return true;
        }
    }
}
