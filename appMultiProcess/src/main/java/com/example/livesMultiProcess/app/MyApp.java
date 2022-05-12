package com.example.livesMultiProcess.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.example.livesMultiProcess.beans.MusicData;
import com.example.livesMultiProcess.service.IMusicView;
import com.example.livesMultiProcess.service.IMusicBinder;
import com.example.livesMultiProcess.service.MusicService;
import com.example.livesMultiProcess.service.MusicViewService;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * 音乐模块支持将音乐服务放在单独进程里，然后view在另一个进程中与其通信
 *
 */

public class MyApp extends Application {

    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }

    public static class MusicServiceProxy {  // 音乐服务代理 全局维护
        private static final String TAG = "MusicServiceProxy";
        static IMusicBinder musicBinder;
        static ServiceConnection conn;

        static List<MusicData> musicDataList;  // 音乐列表全局持有

        static List<GetMusicServiceListener> listeners = new ArrayList<>();  // 异步获取的结果的监听者们

        private static void bindMusicService() {
            conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(TAG, "onServiceConnected");
                    new Thread() {
                        @Override
                        public void run() {
                            musicBinder = IMusicBinder.Stub.asInterface(service);
                            try {
                                musicDataList = musicBinder.getMusicList();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            notifyAllListeners();
                        }
                    }.start();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "onServiceDisconnected");
                    Toast.makeText(getContext(), "对MusicService的连接丢失", Toast.LENGTH_SHORT).show();
                }
            };
            Intent intent = new Intent(context, MusicService.class);
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }

        //        获取代理binder因为有异步存在，所以无论异步还是同步，统一使用回调，不使用返回值
        public static void getMusicBinderAsync() {
            if (musicBinder == null) {  // 异步
                bindMusicService();
            } else {  // 同步
                notifyAllListeners();
            }
        }
        public static void addListener(GetMusicServiceListener listener) {
            listeners.add(listener);
        }
        private static void notifyAllListeners() {
            for (int i = 0; i < listeners.size(); i++) {
                GetMusicServiceListener listener = listeners.get(i);
                if (listener != null)
                    listener.workAfterMusicBinderReady(musicBinder, musicDataList);
            }
        }
        public interface GetMusicServiceListener {
            void workAfterMusicBinderReady(IMusicBinder musicBinder, List<MusicData> musicDataList);
        }
    }

    public static class MusicViewProxy {  // 音乐View服务代理 全局维护 （MusicService使用） 为了使MusicService主动调用，也可以（方案2）在View层设置线程不断取信息并核对，这里选择的方案2
        static IMusicView musicViewBinder;
        static ServiceConnection conn;

        private static void bindMusicViewService(MusicViewProxy.GetMusicViewServiceListener listener) {
            conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    musicViewBinder = IMusicView.Stub.asInterface(service);
                    listener.workAfterMusicViewBinderReady(musicViewBinder);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Toast.makeText(getContext(), "对MusicViewService的连接丢失", Toast.LENGTH_SHORT).show();
                }
            };
            Intent intent = new Intent(context, MusicViewService.class);
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }

        //        获取代理binder因为有异步存在，所以无论异步还是同步，统一使用回调，不使用返回值
        public static void getMusicViewBinderAsync(MusicViewProxy.GetMusicViewServiceListener listener) {
            if (musicViewBinder == null) {  // 异步
                bindMusicViewService(listener);
            } else {  // 同步
                listener.workAfterMusicViewBinderReady(musicViewBinder);
            }
        }

        public interface GetMusicViewServiceListener {
            void workAfterMusicViewBinderReady(IMusicView musicViewBinder);
        }
    }
}
