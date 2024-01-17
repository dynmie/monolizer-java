package me.dynmie.monolizer.player;

import me.dynmie.monolizer.utils.ConsoleUtils;

/**
 * @author dynmie
 */
public class Visualizer {

    private final String[] frames;
    private int currentFrame = 0;

    public Visualizer(String[] frames) {
        this.frames = frames;
    }

    public void setFrame(int frame) {
        currentFrame = frame;
    }

    public int getFrameLength() {
        return frames.length;
    }

    public void printFrame() {
        String out = frames[currentFrame];

        ConsoleUtils.resetCursorPosition();
        System.out.append(out);
        System.out.flush();
    }

}
