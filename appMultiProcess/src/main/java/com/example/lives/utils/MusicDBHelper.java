package com.example.lives.utils;

import android.content.Context;

import com.example.lives.beans.MusicData;
import com.litesuits.orm.LiteOrm;

import java.util.List;

public class MusicDBHelper {
    private static LiteOrm liteOrm = null;
    private static final String ORM_DB_NAME = "musicInfo.db";

    public MusicDBHelper(Context context) {
        if (liteOrm == null)
            liteOrm = LiteOrm.newCascadeInstance(context, ORM_DB_NAME);
    }

    public <T> long insert(T t) {
        return liteOrm.insert(t);
    }

    public <T> List<T> getQueryAll(Class<T> cla) {
        return liteOrm.query(cla);
    }

    public <T> void delete(T t) {
        liteOrm.delete(t);
    }

    public void clear() {
        liteOrm.deleteAll(MusicData.class);
    }
}
