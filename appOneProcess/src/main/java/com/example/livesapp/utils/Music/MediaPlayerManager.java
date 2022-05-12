package com.example.livesapp.utils.Music;

import android.media.MediaPlayer;
import android.util.Log;


import java.io.File;
import java.io.IOException;

/***
 * 主要实现音乐播放状态机
 * 对音乐播放进行控制
 */

public class MediaPlayerManager {

    private static final String TAG = "MediaPlayerManager";
    static volatile MediaPlayerManager ins;

    private MediaPlayer mediaPlayer;
    private STATE mediaState;

    private MediaPlayerManager() {
        mediaPlayer = new MediaPlayer();
        mediaState = STATE.IDLE;
    }

    public static MediaPlayerManager getInstance() {
        if (ins == null) {
            synchronized (MediaPlayerManager.class) {
                if (ins == null) {
                    ins = new MediaPlayerManager();
                }
            }
        }
        return ins;
    }


    private void ensurePathValid(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile())
            throw new RuntimeException("you must give a valid path, but you give " + path);
    }

    /**
     * 定位至某一播放点的前一关键帧（同步帧 sync frame）
     *
     * @param position 单位 ms
     */
    public void seekTo(long position) {
        Log.d(TAG, "seekTo() called with: position = [" + position + "]" + mediaState);
        if (mediaState == STATE.PREPARED || mediaState == STATE.STARTED || mediaState == STATE.PAUSED)
            mediaPlayer.seekTo(position, MediaPlayer.SEEK_CLOSEST_SYNC);
    }

    /**
     * 设置播放结束 监听器
     *
     * @param listener
     */
    public void setOnPlayCompleteListener(MediaPlayer.OnCompletionListener listener) {
        mediaPlayer.setOnCompletionListener(listener);
    }

    /**
     * 返回当前音乐播放位置 单位 ms
     *
     * @return
     */
    public long getCurrentPosition() {
        if (mediaState == STATE.IDLE || mediaState == STATE.INITIALIZED) return -1;
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 返回当前音乐播放总时长 单位 ms
     *
     * @return
     */
    public long getMediaDuration() {
        if (mediaState == STATE.IDLE || mediaState == STATE.INITIALIZED) return -1;
        return mediaPlayer.getDuration();
    }

    /**
     * 判断是否在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return mediaState == STATE.STARTED;
    }

    /**
     * 设置要播放的音乐路径
     *
     * @param path 音乐文件的全路径
     */
    private void setPath(String path) {
        Log.d(TAG, "setPath() called with: path = [" + path + "], mediaState: " + mediaState);
        ensurePathValid(path);
        try {
            if (mediaState == STATE.IDLE) {
                mediaPlayer.setDataSource(path);
                mediaState = STATE.INITIALIZED;
            }
            if (mediaState == STATE.INITIALIZED) {
                mediaPlayer.prepare();
                mediaState = STATE.PREPARED;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置为空闲状态
     */
    public void reset() {
        Log.d(TAG, "reset: " + mediaState);
        mediaPlayer.reset();
        mediaState = STATE.IDLE;
    }

    /**
     * 依赖：歌曲需要先被下载
     * 已有歌曲占用状态机时或不清楚状态机的状态时，使用restart播放新歌曲
     *
     * @param path 音乐文件的全路径
     */
    public void restart(String path) {
        reset();
        setPath(path);
        start();
    }

    /**
     * 播放目标地址下的音乐
     */
    public void start() {
        Log.d(TAG, "start: " + mediaState);
        if (mediaState == STATE.PREPARED || mediaState == STATE.PAUSED) {
            mediaPlayer.start();
            mediaState = STATE.STARTED;
        } else if (mediaState == STATE.STARTED) {

        } else {  // IDLE, INITIALIZED
            throw new RuntimeException("You must set the music path");
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mediaState == STATE.STARTED) {
            mediaPlayer.pause();
            mediaState = STATE.PAUSED;
        }
    }


    //    mediaPlayer本身并没有给出判断它状态的public方法，所有只能在外部记录
    //    状态转换  IDLE   setDataSource   INITIALIZED   prepare   PREPARED   start   STARTED   pause   PAUSED
    //    顺序播放时，当前歌单播放完了，就会进入IDLE状态
    public enum STATE {
        IDLE, INITIALIZED, PREPARED, STARTED, PAUSED
    }
}
