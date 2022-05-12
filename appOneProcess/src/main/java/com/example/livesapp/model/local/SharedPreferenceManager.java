package com.example.livesapp.model.local;

import android.content.SharedPreferences;

import com.example.livesapp.app.MyApp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferenceManager {

    private static String spFileName = MyApp.getContext().getPackageName() + ".SP";
    private static SharedPreferences sp = MyApp.getContext().getSharedPreferences(spFileName, MODE_PRIVATE);

    private static void store(String token, String content, boolean async) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(token, content);
        if (async) editor.apply();
        else editor.commit();
    }

    //    字符串存取
    public static void storeAsync(String token, String content) {
        store(token, content, true);
    }

    public static void store(String token, String content) {
        store(token, content, false);
    }

    public static String get(String token, String defaultValue) {
        return sp.getString(token, defaultValue);
    }

    //    List存取
    public static <T> void storeListAsync(String token, List<T> list) {
        String content = new Gson().toJson(list);
        store(token, content, true);
    }

    public static <T> void storeList(String token, List<T> list) {
        String content = new Gson().toJson(list);
        store(token, content, false);
    }

    public static <T> List<T> getList(String token, List<T> defaultValue) {
        String content = sp.getString(token, null);
        if (content == null) return defaultValue;
        List<T> list = new Gson().fromJson(content, new TypeToken<List<T>>() {
        }.getType());
        return list;
    }
}
