package com.example.livesapp.presenter.Music;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.livesapp.app.MyApp;
import com.example.livesapp.model.beans.MusicData;
import com.example.livesapp.model.network.music.DownloadMusicNetProxy;
import com.example.livesapp.model.network.music.GetMusicListProxy;
import com.example.livesapp.service.MusicForegroundService;
import com.example.livesapp.utils.Music.MediaPlayerManager;
import com.example.livesapp.utils.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 找好并整理音乐开始播放的所有入口
 * 找好并整理音乐停止播放的所有入口
 */

public class MusicPresenter implements IMusicPresenter {

    private static final String TAG = "MusicPresenter";

    private static MusicPresenter ins;
    private MediaPlayerManager mediaPlayerManager;
    private List<Integer> playHistory;
    private PLAY_RULE playRule;
    private Random random;
    private List<MusicData> musicDataList;
    private int curMusicId;  // 播放的是哪个音乐 和 mediaPlayerManager 装载的音乐文件保持一致
    private PLAY_STATE playState;  // 当curMusicId>=0时有效，播放暂停时mediaPlayerManager参与，下载时DownloadMusicNetProxy参与
    private Map<Class<?>, List<?>> updaterMap;
    private volatile List<IMusicList.MusicListUpdater> musicListUpdaters;
    private volatile List<IMusicCommonControl.MusicInfoUpdater> musicInfoUpdaters;
    private volatile List<IMusicCommonControl.MusicStateUpdater> musicStateUpdaters;
    private volatile List<MusicDurationUpdater> musicDurationUpdaters;
    private ProgressUpdateWorker progressUpdateWorker;  // 实时更新进度的线程 // todo 不一定所有界面都需要，可以写成懒加载

    static MusicPresenter getInstance() {
        if (ins == null) {
            synchronized (MusicPresenter.class) {
                if (ins == null) {
                    ins = new MusicPresenter();
                }
            }
        }
        return ins;
    }

    private MusicPresenter() {
        prepareMediaPlayerManager();
        playHistory = new LinkedList<>();
        playRule = PLAY_RULE.SEQUENTIALLY;
        random = new Random();
        prepareMusicDataList();
        curMusicId = -1;
        playState = PLAY_STATE.PAUSING;  // 以暂停的状态初始化
        updaterMap = new HashMap<>();
        musicListUpdaters = new ArrayList<>();
        updaterMap.put(IMusicList.MusicListUpdater.class, musicListUpdaters);
        musicInfoUpdaters = new ArrayList<>();
        updaterMap.put(IMusicCommonControl.MusicInfoUpdater.class, musicInfoUpdaters);
        musicStateUpdaters = new ArrayList<>();
        updaterMap.put(IMusicCommonControl.MusicStateUpdater.class, musicStateUpdaters);
        musicDurationUpdaters = new ArrayList<>();
        updaterMap.put(MusicDurationUpdater.class, musicDurationUpdaters);
        progressUpdateWorker = new ProgressUpdateWorker();
        startForegroundService();
    }

    private void prepareMediaPlayerManager() {
        mediaPlayerManager = MediaPlayerManager.getInstance();
        mediaPlayerManager.setOnPlayCompleteListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextMusic();
            }
        });
    }

    private void prepareMusicDataList() {
        Log.i(TAG, "getMusicList  " + " downloading the musicDataList");
        GetMusicListProxy.getMusicList(new GetMusicListProxy.CallBack() {  // 异步点
            @Override
            public void onSuccess(List<MusicData> musicDataList) {
                Log.i(TAG, "getMusicList  " + " download task of musicDataList finished");
                MusicPresenter.this.musicDataList = musicDataList;
                notifyMusicList(musicDataList);
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        progressUpdateWorker.shutDown();
        progressUpdateWorker = null;
    }

    private void notifyMusicList(List<MusicData> musicDataList) {
        for (int i = 0; i < musicListUpdaters.size(); i++) {
            MusicListUpdater updater = musicListUpdaters.get(i);
            updater.updateMusicList(musicDataList);
        }
    }

    private void notifyMusicInfo(MusicData musicData) {
        for (int i = 0; i < musicInfoUpdaters.size(); i++) {
            MusicInfoUpdater updater = musicInfoUpdaters.get(i);
            updater.updateMusicInfo(musicData);
        }
    }

    private void notifyMusicState(PLAY_STATE playStateTem) {
        Log.d(TAG, "notifyMusicState() called with: playStateTem = [" + playStateTem + "]");
        playState = playStateTem;
        for (int i = 0; i < musicStateUpdaters.size(); i++) {
            MusicStateUpdater updater = musicStateUpdaters.get(i);
            updater.updatePlayState(playState);
        }
    }

    public void notifyMusicDuration(int duration) {
        for (int i = 0; i < musicDurationUpdaters.size(); i++) {
            MusicDurationUpdater updater = musicDurationUpdaters.get(i);
            updater.updateCurMusicDuration(duration);
        }
    }


    /**
     * 启动前台Service
     */
    private void startForegroundService() {
        if (MusicForegroundService.ins == null) {
            Context context = MyApp.getContext();
            Intent intent = new Intent(context, MusicForegroundService.class);
            context.startForegroundService(intent);
        }
    }

    /**
     * 准备并播放
     * 分需要下载（异步）和不需要下载两种情况
     */
    private void prepareAndStart() {
        if (!DownloadMusicNetProxy.isDownloaded(musicDataList.get(curMusicId))) {  // 异步点  如果需要下载
            mediaPlayerManager.pause();
            notifyMusicInfo(musicDataList.get(curMusicId));
            notifyMusicState(PLAY_STATE.DOWNLOADING);
            notifyMusicDuration(-1);
            progressUpdateWorker.notifyCurMusicPosition(-1);
            progressUpdateWorker.stopWork();
            DownloadMusicNetProxy.downloadMusic(musicDataList.get(curMusicId), new DownloadMusicNetProxy.CallBack() {
                @Override
                public void onSuccess(MusicData musicData) {
                    Log.i(TAG, "download Music complete...");
                    if (musicData.equals(musicDataList.get(curMusicId))) {  // 如果下载完了，看当前要播放的是不是这个歌曲
                        String storePath = MyApp.getContext().getFilesDir().getPath();
                        String musicPath = musicData.getPath();
                        mediaPlayerManager.restart(storePath + "/" + musicPath);
                        notifyMusicInfo(musicDataList.get(curMusicId));
                        notifyMusicState(PLAY_STATE.PLAYING);
                        notifyMusicDuration(getCurMusicDuration());
                        progressUpdateWorker.startWork();
                    }
                }

                @Override
                public void onFailed(Throwable e) {
                    Log.i(TAG, "download Music failed...");
                    e.printStackTrace();
                }
            });
        } else {  // 不需要下载
            String storePath = MyApp.getContext().getFilesDir().getPath();
            String musicPath = musicDataList.get(curMusicId).getPath();
            mediaPlayerManager.restart(storePath + "/" + musicPath);
            notifyMusicInfo(musicDataList.get(curMusicId));
            notifyMusicState(PLAY_STATE.PLAYING);
            notifyMusicDuration(getCurMusicDuration());
            progressUpdateWorker.startWork();
        }
    }

    /**
     * 播放
     */
    private void start() {
        mediaPlayerManager.start();
        progressUpdateWorker.startWork();
    }

    /**
     * 顺序播放下一首
     */
    private void sequentNextMusic() {
        if (curMusicId == musicDataList.size() - 1) {  // 最后一首了
//            todo 歌单最后一首音乐结束，该如何处理
            return;
        } else {
            if (curMusicId != -1) playHistory.add(curMusicId);
            curMusicId++;
            prepareAndStart();
        }
    }

    /**
     * 单曲循环播放下一首
     */
    private void singleLoopNextMusic() {
        String storePath = MyApp.getContext().getFilesDir().getPath();
        String musicPath = musicDataList.get(curMusicId).getPath();
        mediaPlayerManager.restart(storePath + "/" + musicPath);
        notifyMusicInfo(musicDataList.get(curMusicId));
        notifyMusicState(PLAY_STATE.PLAYING);
        notifyMusicDuration(getCurMusicDuration());
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

    /*****
     * 以下为公有方法
     */

    /**
     * 获取播放规则
     *
     * @return
     */
    @Override
    public PLAY_RULE getPlayRule() {
        return this.playRule;
    }

    /**
     * 设置播放规则
     *
     * @param playRule
     */
    @Override
    public void setPlayRule(PLAY_RULE playRule) {
        this.playRule = playRule;
    }

    /**
     * 播放curMusicId对应的音乐
     *
     * @return
     */
    @Override
    public void playMusic() {
        if (curMusicId == -1) return;
        playMusic(curMusicId);
    }

    /**
     * 播放musicId对应的音乐
     *
     * @param musicId
     * @return
     */
    @Override
    public void playMusic(int musicId) {
        if (musicId < 0 || musicId >= musicDataList.size()) return;
        if (curMusicId != musicId) {
            curMusicId = musicId;
            prepareAndStart();
        } else {
            if (!mediaPlayerManager.isPlaying())
                start();
        }
        notifyMusicInfo(musicDataList.get(curMusicId));
        notifyMusicState(PLAY_STATE.PLAYING);
        notifyMusicDuration(getCurMusicDuration());
    }

    /**
     * 暂停
     */
    @Override
    public void pauseMusic() {
        if (mediaPlayerManager.isPlaying()) {
            mediaPlayerManager.pause();
            progressUpdateWorker.stopWork();
        }
        notifyMusicState(PLAY_STATE.PAUSING);
    }

    /**
     * 下一首
     */
    @Override
    public void nextMusic() {
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
    }

    /**
     * 上一首
     */
    @Override
    public void lastMusic() {
        if (playHistory.size() == 0) return;
        curMusicId = playHistory.get(playHistory.size() - 1);
        playHistory.remove(playHistory.size() - 1);
        prepareAndStart();
        notifyMusicInfo(musicDataList.get(curMusicId));
        notifyMusicState(PLAY_STATE.PLAYING);
        notifyMusicDuration(getCurMusicDuration());
    }

    /**
     * 选择某一音乐时有效 curMusicId>=0
     * 获取当前播放状态：下载、播放、暂停
     *
     * @return
     */
    @Override
    public PLAY_STATE getPlayState() {
        return playState;
    }

    /**
     * 获取当前歌曲信息
     *
     * @return
     */
    @Override
    public MusicData getMusicInfo() {
        if (curMusicId == -1) return null;
        return musicDataList.get(curMusicId);
    }

    /**
     * 获取当前歌曲列表
     *
     * @return
     */
    @Override
    public List<MusicData> getMusicList() {
        return musicDataList;
    }

    /**
     * 获取当前播放位置 单位 s
     *
     * @return
     */
    @Override
    public int getCurMusicPosition() {
        if (mediaPlayerManager.getCurrentPosition() == -1) return -1;
        return TimeUtil.millisSecondsToSeconds(mediaPlayerManager.getCurrentPosition());
    }

    /**
     * 获取当前歌曲总时长 单位 s
     *
     * @return
     */
    @Override
    public int getCurMusicDuration() {
        if (mediaPlayerManager.getMediaDuration() == -1) return -1;
        return TimeUtil.millisSecondsToSeconds(mediaPlayerManager.getMediaDuration());
    }

    /**
     * 定位到position的位置
     *
     * @param position 单位 s
     */
    @Override
    public void seekTo(int position) {
        mediaPlayerManager.seekTo(TimeUtil.secondsToMillisSeconds(position));
    }

    /**
     * 注册在UI更新音乐列表的逻辑
     *
     * @param updater
     */
    public void registMusicListUpdater(IMusicList.MusicListUpdater updater) {
        musicListUpdaters.add(updater);
    }
//    todo 尝试一下泛型写法，只写一对注册、取消注册函数就行的那种
//    public <T> void registUpdater(T updater) {
//        Class<?> updaterClass = updater.getClass();
//        List<?> updaters = updaterMap.get(updaterClass);
//        updaters.add(updater);
//    }

    /**
     * 取消注册在UI更新音乐列表的逻辑
     *
     * @param updater
     */
    public void unregistMusicListUpdater(IMusicList.MusicListUpdater updater) {
        musicListUpdaters.remove(updater);
    }

    /**
     * 注册在UI更新音乐信息的逻辑
     *
     * @param updater
     */
    @Override
    public void registMusicInfoUpdater(IMusicCommonControl.MusicInfoUpdater updater) {
        musicInfoUpdaters.add(updater);
    }

    /**
     * 取消注册在UI更新音乐信息的逻辑
     *
     * @param updater
     */
    @Override
    public void unregistMusicInfoUpdater(IMusicCommonControl.MusicInfoUpdater updater) {
        musicInfoUpdaters.remove(updater);
    }

    /**
     * 注册在UI更新音乐状态的逻辑
     *
     * @param updater
     */
    @Override
    public void registMusicStateUpdater(IMusicCommonControl.MusicStateUpdater updater) {
        musicStateUpdaters.add(updater);
    }

    /**
     * 取消注册在UI更新音乐状态的逻辑
     *
     * @param updater
     */
    @Override
    public void unregistMusicStateUpdater(IMusicCommonControl.MusicStateUpdater updater) {
        musicStateUpdaters.remove(updater);
    }

    /**
     * 注册在UI更新音乐时长的逻辑
     *
     * @param updater
     */
    public void registDurationUpdater(IMusicProgress.MusicDurationUpdater updater) {
        musicDurationUpdaters.add(updater);
    }

    /**
     * 取消注册在UI更新音乐时长的逻辑
     *
     * @param updater
     */
    public void unregistDurationUpdater(IMusicProgress.MusicDurationUpdater updater) {
        musicDurationUpdaters.remove(updater);
    }

    /**
     * 注册在UI更新音乐播放进度的逻辑
     *
     * @param updater
     */
    public void registProgressUpdater(IMusicProgress.ProgressUpdater updater) {
        progressUpdateWorker.registListener(updater);
    }

    /**
     * 取消注册在UI更新音乐播放进度的逻辑
     *
     * @param updater
     */
    public void unregistProgressUpdater(IMusicProgress.ProgressUpdater updater) {
        progressUpdateWorker.unregistListener(updater);
    }

    /**
     * 实时更新音乐播放进度的线程
     */
    private class ProgressUpdateWorker extends Thread {
        private boolean running;  // 线程执行looper
        private List<IMusicProgress.ProgressUpdater> listeners;
        private ReentrantLock lock;
        private Condition noTask;  // 无任务睡眠
        private boolean haveTask;

        public ProgressUpdateWorker() {
            super();
            running = true;
            listeners = new ArrayList<>();
            lock = new ReentrantLock();
            noTask = lock.newCondition();
            haveTask = false;
            start();  // 启动线程自己
        }

        @Override
        public void run() {
            while (running) {
                lock.lock();
                while (!haveTask) {
                    try {
                        noTask.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                lock.unlock();
                notifyCurMusicPosition(getCurMusicPosition());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void notifyCurMusicPosition(int curPosition) {
            for (int i = 0; i < listeners.size(); i++) {
                IMusicProgress.ProgressUpdater listener = listeners.get(i);
                listener.updateCurMusicPosition(curPosition);
            }
        }

        public void registListener(IMusicProgress.ProgressUpdater listener) {
            listeners.add(listener);
            if (getPlayState() == PLAY_STATE.PLAYING) startWork();
        }

        public void unregistListener(IMusicProgress.ProgressUpdater listener) {
            listeners.remove(listener);
            if (!haveListener()) stopWork();
        }

        public void shutDown() {
            running = false;
        }

        private boolean haveListener() {
            return !listeners.isEmpty();
        }

        /**
         * 开始工作，不保证真的在工作，有listener监听才工作
         * 播放音乐时调用
         */
        public void startWork() {  // updater工作有两个必备条件，一个是有界面监听进度，一个是音乐在播放
            if (!haveListener()) return;
            haveTask = true;
            lock.lock();
            noTask.signalAll();
            lock.unlock();
        }

        /**
         * 停止工作
         * 暂停音乐时调用
         */
        public void stopWork() {  // 否则 updater不工作
            haveTask = false;
        }
    }
}
