package me.dynmie.monolizer.player;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * @author dynmie
 */
public class AudioPlayer {

    private final Clip clip;

    public AudioPlayer(File file) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(stream);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }

    public Clip getClip() {
        return clip;
    }

    public void play() {
        clip.start();
    }

    public void pause() {
        clip.stop();
    }

}
