package com.example.livesapp.model.network.translate;

import com.example.livesapp.model.beans.TranslateData;
import com.example.livesapp.model.network.NetAPIs;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;   // 这里需要RxAndroid，RxAndroid 是专为Android设计的RxJava扩展
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
//import rx.Subscriber; // 这是版本1的用法，不要版本2和版本1混用


public class TranslateNetProxy {
    public interface CallBack {
        void onSuccess(TranslateData translateData);
        void onFailed(Throwable e);
    }

    public static void translate(String content, CallBack callBack) {
        Observable<TranslateData> observable = NetAPIs.translateApi.getTranslateData("allynlive", "650717794", "data", "json", "1.1", content);
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<TranslateData>() {
                    @Override
                    public void accept(TranslateData translateData) throws Exception {
                        callBack.onSuccess(translateData);
                    }
                });
    }
}
