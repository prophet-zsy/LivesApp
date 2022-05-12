package com.example.livesapp.utils;

import com.example.livesapp.utils.Music.MediaPlayerManager;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class MediaPlayerManagerTest {

    MediaPlayerManager mediaPlayerManager;

    @Before
    public void setup() {
        mediaPlayerManager = MediaPlayerManager.getInstance();
    }

    @Test
    public void getInstance() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                MediaPlayerManager ins = MediaPlayerManager.getInstance();
                assertEquals(mediaPlayerManager, ins);
            }
        };
        for (int i = 0; i < 100; i++) {
            executorService.submit(task);
        }
        executorService.shutdown();
    }
}
