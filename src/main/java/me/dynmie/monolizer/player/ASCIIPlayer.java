package me.dynmie.monolizer.player;

import java.io.File;

/**
 * @author dynmie
 */
public class ASCIIPlayer {

    private final Visualizer visualizer;
    private final AudioPlayer audioPlayer;
    private boolean playing;

    public ASCIIPlayer(Visualizer visualizer, AudioPlayer audioPlayer) {
        this.visualizer = visualizer;
        this.audioPlayer = audioPlayer;
    }

    public ASCIIPlayer(String[] frames, File audioFile) {
        this(
                new Visualizer(frames),
                new AudioPlayer(audioFile)
        );
    }

    private void run() {
        Thread.startVirtualThread(() -> {
            while (isPlaying()) {
                if (isDone()) {
                    break;
                }

                double percentage = (double) audioPlayer.getClip().getFramePosition() / audioPlayer.getClip().getFrameLength();
                int currentFrame = (int) (percentage * (visualizer.getFrameLength() - 1));
                visualizer.setFrame(currentFrame);
                visualizer.printFrame();

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void play() {
        if (playing) return;
        audioPlayer.play();
        playing = true;
        run();
    }

    public void pause() {
        audioPlayer.pause();
        playing = false;
    }

    public boolean isPlaying() {
        return audioPlayer.getClip().isActive();
    }

    public boolean isDone() {
        return audioPlayer.getClip().getFramePosition() >= audioPlayer.getClip().getFrameLength();
    }

}
