package com.soundbound.player;

import com.soundbound.SimpleSong;

public interface Player {
    void resume();
    void playAt(long time);
    void pause();
    void play(SimpleSong ss);
}
