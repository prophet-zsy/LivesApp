package com.example.livesMultiProcess.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LiveRetrofit {

    private Retrofit retrofit;

    LiveRetrofit(String url) {
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())  // 添加json解析器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())  // retrofit适配rxJava（主要是接口返回值可以为observable）
                .build();
    }

    public <T> T getLiveService(Class<T> tClass) {
        return retrofit.create(tClass);
    }
}
