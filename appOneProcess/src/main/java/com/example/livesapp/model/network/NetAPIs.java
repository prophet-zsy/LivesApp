package com.example.livesapp.model.network;

public class NetAPIs {
//    url
    private static final String translateUrl = "https://fanyi.youdao.com/";
//    通过retrofit创建的api
    public static final NetAPI.TranslateNetAPI translateApi = new LiveRetrofit(translateUrl).getLiveService(NetAPI.TranslateNetAPI.class);

//    url
    private static final String musicUrl = "http://prophet-zsy.cc:8060/MusicInfoManageSystem/";
//    通过retrofit创建的api
    public static final NetAPI.MusicNetAPI musicApi = new LiveRetrofit(musicUrl).getLiveService(NetAPI.MusicNetAPI.class);
}
