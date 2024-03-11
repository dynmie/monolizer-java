package me.dynmie.monolizer.timer;

/**
 * @author dynmie
 */
public class StartPlaybackTimer implements PlaybackTimer {

    private long start = -1L;

    @Override
    public void init() {
        start = System.nanoTime();
    }

    @Override
    public long elapsedMicros() {
        if (start < 0) {
            throw new IllegalArgumentException("timer not initialized");
        }
        return (System.nanoTime() - start) / 1000;
    }
}
