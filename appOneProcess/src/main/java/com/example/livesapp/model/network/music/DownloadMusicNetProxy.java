package com.example.livesapp.model.network.music;

import com.example.livesapp.app.MyApp;
import com.example.livesapp.model.beans.MusicData;
import com.example.livesapp.model.network.NetAPIs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class DownloadMusicNetProxy {
    public static boolean isDownloaded(MusicData musicData) {
        String storePath = MyApp.getContext().getFilesDir().getPath();
        String musicPath = musicData.getPath();
        File file = new File(storePath, musicPath);
        return file.exists() && file.isFile();
    }

    public interface CallBack {
        void onSuccess(MusicData musicData);
        void onFailed(Throwable e);
    }

    public static void downloadMusic(MusicData musicData, CallBack callBack) {
        String storePath = MyApp.getContext().getFilesDir().getPath();
        String musicPath = musicData.getPath();
        Observable<ResponseBody> observable = NetAPIs.musicApi.getMusicData(musicPath);
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(final ObservableEmitter<Object> e) throws Exception {
                observable  // 因为要写文件，所以不切换主线程
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<ResponseBody>() {
                            @Override
                            public void accept(ResponseBody responseBody) throws Exception {
                                InputStream inputStream = responseBody.byteStream();
                                OutputStream outputStream = new FileOutputStream(new File(storePath, musicPath));
                                byte[] buf = new byte[1024];
                                int len = -1;
                                while ((len = inputStream.read(buf)) > 0) {
                                    outputStream.write(buf, 0, len);
                                }
                                inputStream.close();
                                outputStream.close();
                                e.onComplete();
                            }
                        });
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object value) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callBack.onFailed(e);
                    }

                    @Override
                    public void onComplete() {
                        callBack.onSuccess(musicData);
                    }
                });
    }
}
