package com.example.livesapp.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ServiceTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class MusicForegroundServiceTest {

    private static final String TAG = "MusicForegroundServiceT";

    @Rule
    public ServiceTestRule rule = new ServiceTestRule();
    private Context appContext;
    private MusicForegroundService service;

    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void notificationTest() {  // 这里尚未找到测试notification的接口（看看espresso中有没有），可以通过暂停观察的方式来手动测试
        Intent intent = new Intent(appContext, MusicForegroundService.class);
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicForegroundServiceTest.this.service = ((MusicForegroundService.MyBinder) service).getService();
                assertFalse(MusicForegroundService.ins == null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: ");
            }
        };
        appContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}