package com.example.livesMultiProcess.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;


public class MusicViewService extends Service {
    public MusicViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicViewBinder();
    }

    public class MusicViewBinder extends IMusicView.Stub {

        @Override
        public boolean setCurMusicUI(int curMusicId) throws RemoteException {
            return false;
        }
    }
}
