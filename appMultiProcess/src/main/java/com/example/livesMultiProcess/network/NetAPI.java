package com.example.livesMultiProcess.network;

import com.example.livesMultiProcess.beans.TranslateData;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NetAPI {

    public interface TranslateNetAPI {
//    按照http协议get方法中的请求参数进行定义
//    返回一个Observable对象，当观察的事件发生后，网络上返回的json数据通过retrofit创建时配置的gson来解析成TranslateData对象
        @GET("openapi.do")
        public Observable<TranslateData> getTranslateData(@Query("keyfrom") String keyfrom, @Query("key") String key, @Query("type") String type, @Query("doctype") String doctype, @Query("version") String version, @Query("q") String q);
    }

    public interface MusicNetAPI {
        @GET("download-servlet")
        public Observable<ResponseBody> getMusicData(@Query("musicName") String musicName);

        @GET("getMusicList-servlet")
        public Observable<ResponseBody> getMusicList();
    }
}
