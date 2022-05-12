package com.example.livesMultiProcess.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


/**
 * 网络请求使用
 */

public class TranslateData implements Parcelable {

    private String query;
    private List<String> translation;
    private Basic basic;
    private int errorCode;
    private List<WebItem> web;

    public static class Basic {
//        String us-phonetic;  // todo 这些带下划线的字段该如何处理
//        String phonetic;   // 这三个字段不是很要紧
//        String uk-phonetic;
          List<String> explains;

        public List<String> getExplains() {
            return explains;
        }

        public void setExplains(List<String> explains) {
            this.explains = explains;
        }
    }

    public static class WebItem {  // todo 不需要保证它也实现 Parcelable么？
        private String key;
        private List<String> value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public List<String> getValue() {  // List自己重写了toString函数
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }
    }

    public List<String> getTranslation() {
        return translation;
    }

    public void setTranslation(List<String> translation) {
        this.translation = translation;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Basic getBasic() {
        return basic;
    }

    public void setBasic(Basic basic) {
        this.basic = basic;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public List<WebItem> getWeb() {
        return web;
    }

    public void setWeb(List<WebItem> web) {
        this.web = web;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {  // 序列化
        dest.writeList(translation);
        dest.writeString(query);
        dest.writeInt(errorCode);
        dest.writeList(web);
    }

    public TranslateData(Parcel source) {  // 传入Parcel对象进行反序列化
        this.translation = new ArrayList<String>();
        source.readList(this.translation, List.class.getClassLoader());
        this.query = source.readString();
        this.errorCode = source.readInt();
        this.web = new ArrayList<WebItem>();
        source.readList(this.web, List.class.getClassLoader());
    }

    public static final Creator CREATOR = new Creator() {  // 通过CREATOR进行反序列化，该字段名只能为CREATOR
        @Override
        public Object createFromParcel(Parcel source) {
            return new TranslateData(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new Object[0];
        }
    };
}
