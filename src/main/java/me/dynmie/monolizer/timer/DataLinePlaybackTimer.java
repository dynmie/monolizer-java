package me.dynmie.monolizer.timer;

import javax.sound.sampled.DataLine;

/**
 * @author dynmie
 */
public class DataLinePlaybackTimer implements PlaybackTimer {

    private final DataLine audioLine;

    public DataLinePlaybackTimer(DataLine audioLine) {
        this.audioLine = audioLine;
    }

    @Override
    public void init() {}

    @Override
    public long elapsedMicros() {
        return audioLine.getMicrosecondPosition();
    }

}
