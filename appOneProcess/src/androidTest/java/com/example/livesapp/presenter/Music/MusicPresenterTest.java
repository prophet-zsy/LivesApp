package com.example.livesapp.presenter.Music;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.livesapp.presenter.Music.MusicPresenter;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class MusicPresenterTest {
    private Context myApp;
    private MusicPresenter musicPresenter;

    @Before
    public void setup() {
        myApp = InstrumentationRegistry.getInstrumentation().getTargetContext();
        musicPresenter = MusicPresenter.getInstance();
    }

    @Test
    public void getInstance() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                MusicPresenter ins = MusicPresenter.getInstance();
                assertEquals(musicPresenter, ins);
            }
        };
        for (int i = 0; i < 100; i++) {
            executorService.submit(task);
        }
        executorService.shutdown();
    }

    @Test
    public void getPlayRule() {

    }

    @Test
    public void setPlayRule() {
    }

    @Test
    public void playMusic() {
    }

    @Test
    public void playMusic1() {
    }

    @Test
    public void pauseMusic() {
    }

    @Test
    public void nextMusic() {
//        switch (musicPresenter.getPlayRule()) {
//            case SEQUENTIALLY:
//                int size = musicPresenter.getMusicList().size();
//                int lastMusicId = musicPresenter.getMusicInfo().getId();
//                musicPresenter.nextMusic();
//                assertEquals(true, musicPresenter.getPlayState());
//                assertEquals( musicPresenter.getMusicInfo().getId());
//                break;
//            case SINGLE_LOOP:
//                singleLoopNextMusic();
//                break;
//            case RANDOM:
//                randomNextMusic();
//                break;
//        }
    }

    @Test
    public void lastMusic() {
    }

    @Test
    public void isPlaying() {
    }

    @Test
    public void getMusicInfo() {
    }

    @Test
    public void getMusicList() {
    }

    @Test
    public void getCurMusicPosition() {
    }

    @Test
    public void getCurMusicDuration() {
    }

    @Test
    public void seekTo() {
    }
}