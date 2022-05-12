package com.example.livesapp.model.network.music;

import com.example.livesapp.model.beans.MusicData;
import com.example.livesapp.model.network.NetAPIs;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class GetMusicListProxy {
    public interface CallBack {
        void onSuccess(List<MusicData> musicDataList);
        void onFailed(Exception e);
    }

    private static final String TAG = "GetMusicListProxy";

    public static void getMusicList(CallBack callBack) {
        Observable<ResponseBody> observable = NetAPIs.musicApi.getMusicList();
        try {
            observable.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<ResponseBody>() {
                        @Override
                        public void accept(ResponseBody responseBody) throws Exception {
                            String content = responseBody.string();
                            String[] names = content.split(",");
                            List<MusicData> musicDataList = new ArrayList<>();
                            for (int i = 0; i < names.length; i ++) {
                                String name = names[i];
                                if (name.endsWith(".mp3")) {
                                    musicDataList.add(new MusicData(i, name.substring(0, name.length()-4), "", name));
                                }
                            }
                            callBack.onSuccess(musicDataList);
                        }
                    });
        } catch (Exception e) {
            callBack.onFailed(e);
        }
    }
}
