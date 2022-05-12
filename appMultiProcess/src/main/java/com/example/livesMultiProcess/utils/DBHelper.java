package com.example.livesMultiProcess.utils;

import android.content.Context;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.List;

public class DBHelper {

    private static LiteOrm liteOrm = null;
    private static final String ORM_DB_NAME = "stepCount.db";

    public DBHelper(Context context) {
        if (liteOrm == null)
            liteOrm = LiteOrm.newCascadeInstance(context, ORM_DB_NAME);
    }

    public <T> long insert(T t) {
        return liteOrm.insert(t);
    }

    public <T> void delete(T t) {
        liteOrm.delete(t);
    }

    public <T> long save(T t) {
        return liteOrm.save(t);
    }

    public <T> void update(T t) {
        liteOrm.update(t);
    }

    public <T> List<T> getQueryAll(Class<T> cla) {
        return liteOrm.query(cla);
    }

    public <T> List<T> getQueryByWhere(Class<T> cla, String field, Object[] value) {
        return liteOrm.<T>query(new QueryBuilder<T>(cla).where(field + "=?", value));
    }

    public <T> List<T> getQueryByWhereLimit(Class<T> cla, String field, Object[] value, int start, int length) {
        return liteOrm.<T>query(new QueryBuilder<T>(cla).where(field + "=?", value).limit(start, length));
    }
}
