package com.soundbound.View.player;

import com.soundbound.outer.API.songProviders.Models.SimpleSong;

public interface Player {
    void resume();
    void playAt(long time);
    void pause();
    void play(SimpleSong ss);
}
