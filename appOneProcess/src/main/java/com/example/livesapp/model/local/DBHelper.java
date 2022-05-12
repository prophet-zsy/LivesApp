package com.example.livesapp.model.local;

import android.content.Context;

import com.example.livesapp.app.MyApp;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.List;

public class DBHelper {

    private static volatile DBHelper ins;
    private static LiteOrm liteOrm;
    private static String ORM_DB_NAME;

    public static DBHelper getInstance() {
        if (ins == null) {
            synchronized (DBHelper.class) {
                if (ins == null) {
                    ins = new DBHelper();
                }
            }
        }
        return ins;
    }

    public DBHelper() {
        ORM_DB_NAME = MyApp.getContext().getPackageName() + ".data.db";
        liteOrm = LiteOrm.newSingleInstance(MyApp.getContext(), ORM_DB_NAME);
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
