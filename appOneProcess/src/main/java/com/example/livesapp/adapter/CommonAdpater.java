package com.example.livesapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.livesMultiProcess.R;

import java.util.List;

public abstract class CommonAdpater<T> extends BaseAdapter {
    private List<T> data;
    private Context context;
    private int layoutId;

    public CommonAdpater(List<T> data, Context context, int layoutId) {
        this.data = data;
        this.context = context;
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.sport_history_list_item, parent, false);
        }
        T dataItem = getItem(position);
        convertView(convertView, dataItem); // 装填数据
        return convertView;
    }

    protected abstract void convertView(View view, T t);

}
