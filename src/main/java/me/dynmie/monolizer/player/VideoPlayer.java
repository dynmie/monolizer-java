package me.dynmie.monolizer.player;

import me.dynmie.monolizer.timer.PlaybackTimer;
import me.dynmie.monolizer.utils.ConsoleUtils;
import me.dynmie.monolizer.utils.FrameUtils;
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author dynmie
 */
public class VideoPlayer {

    private final File source;
    private final int width;
    private final int height;
    private final boolean resize;
    private final boolean color;

    private volatile boolean running = true;
    private volatile boolean paused;

    public VideoPlayer(File source, int width, int height, boolean resize, boolean color) {
        this.source = source;
        this.width = width;
        this.height = height;
        this.resize = resize;
        this.color = color;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void play() {
        paused = false;
        notifyAll();
    }

    public void pause() {
        paused = true;
    }

    public void stop() {
        running = false;
    }

    public void start() {
        ConsoleUtils.setCursorVisibility(false);

        try (
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(source);
                Java2DFrameConverter converter = new Java2DFrameConverter()
        ) {
            if (resize) {
                grabber.setImageScalingFlags(swscale.SWS_BICUBIC);
                grabber.setImageWidth(width);
                grabber.setImageHeight(height / 2);
            }

            grabber.start();

            PlaybackTimer playbackTimer;
            SourceDataLine audioLine;
            if (grabber.getAudioChannels() > 0) {
                AudioFormat audioFormat = new AudioFormat(
                        grabber.getSampleRate(),
                        16,
                        grabber.getAudioChannels(),
                        true,
                        true
                );
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                audioLine = (SourceDataLine) AudioSystem.getLine(info);
                audioLine.open(audioFormat);
                audioLine.start();

                playbackTimer = PlaybackTimer.create(audioLine);
            } else {
                audioLine = null;
                playbackTimer = PlaybackTimer.create();
            }

            ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
            ExecutorService audioExecutor = Executors.newSingleThreadExecutor();

            long maxReadAheadBufferMicros = TimeUnit.MILLISECONDS.toMicros(1000);

            PrintWriter writer = new PrintWriter(System.out, false);

            long lastTimestamp = -1L;

            while (!Thread.interrupted() && running) {
                synchronized (this) {
                    while (paused) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }

                Frame frame = grabber.grab();
                if (frame == null) break;

                if (lastTimestamp == -1L) {
                    playbackTimer.init();
                }

                lastTimestamp = frame.timestamp;

                // if frame is a video frame
                if (frame.image != null) {
                    Frame imageFrame = frame.clone();

                    final boolean col = color;

                    imageExecutor.submit(() -> {
                        // sync video with audio
                        long delayMicros = imageFrame.timestamp - playbackTimer.elapsedMicros();

                        // if video is faster than audio
                        if (delayMicros > 0) {
                            // wait for audio to catch up with the video
                            try {
                                Thread.sleep(TimeUnit.MICROSECONDS.toMillis(delayMicros));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        if (delayMicros < 0) return; // we're behind! skip the frame.

                        BufferedImage image = converter.convert(imageFrame);
                        imageFrame.close();
                        String text = FrameUtils.convertFrameToText(image, col);

                        ConsoleUtils.resetCursorPosition();
                        writer.append(text);
                        writer.flush();
                    });
                } else if (frame.samples != null) { // if frame is audio frame
                    if (audioLine == null) {
                        throw new IllegalStateException("audio line was not initialized!");
                    }

                    ShortBuffer sampleShortBuffer = (ShortBuffer) frame.samples[0];
                    sampleShortBuffer.rewind();

                    ByteBuffer outBuffer = ByteBuffer.allocate(sampleShortBuffer.capacity() * 2);

                    for (int i = 0; i < sampleShortBuffer.capacity(); i++) {
                        short val = sampleShortBuffer.get(i);
                        outBuffer.putShort(val);
                    }

                    audioExecutor.submit(() -> {
                        audioLine.write(outBuffer.array(), 0, outBuffer.capacity());
                        outBuffer.clear();
                    });
                }


                // ensure that the audio doesn't go faster than the video
                long timeStampDeltaMicros = frame.timestamp - playbackTimer.elapsedMicros();
                if (timeStampDeltaMicros > maxReadAheadBufferMicros) {
                    Thread.sleep(TimeUnit.MICROSECONDS.toMillis(timeStampDeltaMicros - maxReadAheadBufferMicros));
                }
            }

            grabber.stop();
            grabber.release();

            if (audioLine != null) {
                audioLine.stop();
            }

            audioExecutor.shutdownNow();
            audioExecutor.awaitTermination(10, TimeUnit.SECONDS);

            imageExecutor.shutdownNow();
            imageExecutor.awaitTermination(10, TimeUnit.SECONDS);

            ConsoleUtils.setCursorVisibility(true);
        } catch (FrameGrabber.Exception | LineUnavailableException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
