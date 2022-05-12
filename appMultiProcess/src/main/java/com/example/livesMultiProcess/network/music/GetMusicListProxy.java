package com.example.livesMultiProcess.network.music;

import android.util.Log;

import com.example.livesMultiProcess.beans.MusicData;
import com.example.livesMultiProcess.network.NetAPIs;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class GetMusicListProxy {
    private static final String TAG = "GetMusicListProxy";
    public static void getMusicList(MusicListListener listener) {
        Observable<ResponseBody> observable = NetAPIs.musicApi.getMusicList();
        observable.observeOn(AndroidSchedulers.mainThread())
                  .subscribeOn(Schedulers.io())
                  .subscribe(new Consumer<ResponseBody>() {
                      @Override
                      public void accept(ResponseBody responseBody) throws Exception {
                          Log.i(TAG, "getMusicList  " + " downloading the musicDataList");
                          String content = responseBody.string();
                          String[] names = content.split(",");
                          List<MusicData> musicDataList = new ArrayList<>();
                          for (int i = 0; i < names.length; i ++) {
                              String name = names[i];
                              if (name.endsWith(".mp3")) {
                                  musicDataList.add(new MusicData(name.substring(0, name.length()-4), "", name));
                              }
                          }
                          listener.setMusicDataList(musicDataList);
                          Log.i(TAG, "getMusicList  " + " download task of musicDataList finished");
                      }
                  });
    }
    public interface MusicListListener {
        void setMusicDataList(List<MusicData> musicDataList);
    }
}
