// IMusicBinder.aidl
package com.example.livesMultiProcess.service;

// Declare any non-default types here with import statements
import com.example.livesMultiProcess.beans.MusicData;

// you mustn't type the chinese character in the aidl file

/**
* nextMusic lastMusic  which one to play is determined by MusicService
* playMusic  which one to play is determined by view level
*/

interface IMusicBinder {
    List<MusicData> getMusicList();
    boolean isPlaying();
    boolean playMusic(int musicId);
    boolean pauseMusic();
    int nextMusic();  // return the next song's id
    int lastMusic();  // return the last song's id
    boolean seekTo(int position);
    int getCurMusicId();
    int getCurMusicPosition();
    int getCurMusicDuration();
    //    SEQUENTIALLY(0), SINGLE_LOOP(1), RANDOM(2)
    int getPlayRule();
    boolean setPlayRule(int playRule);
}
