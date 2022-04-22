package com.example.lives.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.lives.R;
import com.example.lives.beans.MusicData;

public class PlayerTab extends RelativeLayout {

    TextView musicName;
    TextView musicAuthor;
    ImageView play;
    ImageView lastMusic;
    ImageView nextMusic;

    public PlayerTab(Context context) {
        super(context);
        init(context);
    }

    public PlayerTab(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.player_tab, this);
        musicName = findViewById(R.id.music_name);
        musicAuthor = findViewById(R.id.music_author);
        play = findViewById(R.id.play);
        lastMusic = findViewById(R.id.last_music);
        nextMusic = findViewById(R.id.next_music);
    }

    public void setPlayOnClickListener(View.OnClickListener listener) {
        play.setOnClickListener(listener);
    }
    public void setLastMusicOnClickListener(View.OnClickListener listener) {
        lastMusic.setOnClickListener(listener);
    }
    public void setNextMusicOnClickListener(View.OnClickListener listener) {
        nextMusic.setOnClickListener(listener);
    }

    public void setMusicInfo(MusicData musicInfo) {
        musicName.setText(musicInfo.getName());
        musicAuthor.setText(musicInfo.getAuthor());
    }
    public void setStatePlay(boolean playing) {
        if (playing) play.setImageResource(R.mipmap.ic_pause);
        else play.setImageResource(R.mipmap.ic_play);
    }
}
