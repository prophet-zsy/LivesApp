package com.example.livesapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.livesMultiProcess.R;
import com.example.livesapp.model.beans.MusicData;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {

    private List<MusicData> musicDataList;
    private Context context;

    public MusicListAdapter(List<MusicData> musicDataList, Context context) {
        this.musicDataList = musicDataList;
        this.context = context;
    }

    public void updateMusicList(List<MusicData> List) {
        musicDataList = List;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return musicDataList == null ? 0 : musicDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicDataList == null ? null : musicDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.music_item, null);
        }
        TextView musicName = convertView.findViewById(R.id.musicName);
        TextView authorName = convertView.findViewById(R.id.authorName);
        musicName.setText(musicDataList.get(position).getName());
        authorName.setText(musicDataList.get(position).getAuthor());
        return convertView;
    }
}
