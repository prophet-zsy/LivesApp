package com.example.livesMultiProcess.beans;


import android.os.Parcel;
import android.os.Parcelable;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * 读数据库使用
 */
@Table("MusicData")
public class MusicData implements Parcelable {
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    @Column("id")
    private int id;
    @Column("name")
    private String name;
    @Column("author")
    private String author;
    @Column("path")
    private String path;  // 音乐来自云端，未下载时，path为""，下载后有实际值

    public MusicData() {
    }

    public MusicData(String name, String author, String path) {
        this.name = name;
        this.author = author;
        this.path = path;
    }

    public MusicData(int id, String name, String author, String path) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.path = path;
    }

    protected MusicData(Parcel in) {
        id = in.readInt();
        name = in.readString();
        author = in.readString();
        path = in.readString();
    }

    public static final Creator<MusicData> CREATOR = new Creator<MusicData>() {
        @Override
        public MusicData createFromParcel(Parcel in) {
            return new MusicData(in);
        }

        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(path);
    }
}
