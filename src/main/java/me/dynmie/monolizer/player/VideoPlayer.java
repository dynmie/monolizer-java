package me.dynmie.monolizer.player;

import me.dynmie.monolizer.timer.PlaybackTimer;
import me.dynmie.monolizer.utils.ConsoleUtils;
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dynmie
 */
public class VideoPlayer {

    private final OutputStream outputStream;
    private final File source;
    private volatile int width;
    private volatile int height;
    private volatile Asciifier asciifier;

    private volatile boolean running = false;
    private volatile boolean paused = false;

    private Thread thread;

    public VideoPlayer(OutputStream outputStream, File source, int width, int height, Asciifier asciifier) {
        this.outputStream = outputStream;
        this.source = source;
        this.width = width;
        this.height = height;
        this.asciifier = asciifier;
    }

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void play() {
        paused = false;
        synchronized (this) {
            notifyAll();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Asciifier getAsciifier() {
        return asciifier;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setAsciifier(Asciifier asciifier) {
        this.asciifier = asciifier;
    }

    public void pause() {
        paused = true;
    }

    public void stop() {
        running = false;
        synchronized (this) {
            notifyAll();
        }
    }

    private void run() {
        try (
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(source);
                Java2DFrameConverter converter = new Java2DFrameConverter()
        ) {
            grabber.setImageScalingFlags(swscale.SWS_BICUBIC);
            grabber.setImageWidth(width);
            grabber.setImageHeight(height);

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

            ExecutorService renderingExecutor = Executors.newSingleThreadExecutor();
            ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
            ExecutorService audioExecutor = Executors.newSingleThreadExecutor();

            long maxReadAheadBufferMicros = TimeUnit.MILLISECONDS.toMicros(500);

            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream), 1080 * 720 * 22), false);

            long lastTimestamp = -1L;

            AtomicInteger atomicQueueSize = new AtomicInteger(0);

            while (!Thread.interrupted() && running) {
                synchronized (this) {
                    while (paused) {
                        // NOTE: PAUSING WITHOUT AUDIO DOES NOT WORK.
                        if (audioLine != null) {
                            audioLine.stop();
                        }
                        wait();
                        if (!running) {
                            return;
                        }
                        if (audioLine != null) {
                            audioLine.start();
                        }
                    }
                }

                if (width != grabber.getImageWidth() || height != grabber.getImageHeight()) {
                    grabber.setImageWidth(width);
                    grabber.setImageHeight(height);
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

                    renderingExecutor.submit(() -> {
                        if (width != imageFrame.imageWidth || height != imageFrame.imageHeight) {
                            return;
                        }

                        // sync video with audio
                        long preDelayMicros = imageFrame.timestamp - playbackTimer.elapsedMicros();
                        if (preDelayMicros < 0) return; // we're behind! skip the frame.

                        BufferedImage image = converter.convert(imageFrame);
                        imageFrame.close();

                        String prefix = "";
                        if (!asciifier.isColor()) {
                            prefix += ConsoleUtils.getForegroundResetCode();
                        } else if (!asciifier.isFullPixel()) {
                            prefix += ConsoleUtils.getBackgroundResetCode();
                        }

                        String text = prefix + ConsoleUtils.getResetCursorPositionEscapeCode() + asciifier.createFrame(image);

                        atomicQueueSize.incrementAndGet();
                        imageExecutor.submit(() -> {
                            int queueSize = atomicQueueSize.getAndDecrement();

                            if (width != imageFrame.imageWidth || height != imageFrame.imageHeight) {
                                return;
                            }

                            // sync video with audio
                            long delayMicros = imageFrame.timestamp - playbackTimer.elapsedMicros();
                            if (delayMicros < 0 && queueSize > 1) return; // we're behind! skip the frame.

                            // recalculate delta
                            delayMicros = imageFrame.timestamp - playbackTimer.elapsedMicros();
                            // if video is faster than audio
                            if (delayMicros > 0) {
                                // wait for audio to catch up with the video
                                try {
                                    Thread.sleep(TimeUnit.MICROSECONDS.toMillis(delayMicros));
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            // if paused
                            if (paused) {
                                synchronized (this) {
                                    try {
                                        wait();
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                if (!running) {
                                    return;
                                }
                            }

                            writer.write(text);
                            writer.flush();
                        });
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

                        if (paused) {
                            synchronized (this) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (!running) {
                                return;
                            }
                        }

                        audioLine.write(outBuffer.array(), 0, outBuffer.capacity());
                        outBuffer.clear();
                    });
                }


                // ensure that the audio doesn't go faster than the getVideos
                long timeStampDeltaMicros = frame.timestamp - playbackTimer.elapsedMicros();
                if (timeStampDeltaMicros > maxReadAheadBufferMicros) {
                    Thread.sleep(TimeUnit.MICROSECONDS.toMillis(timeStampDeltaMicros - maxReadAheadBufferMicros));
                }
            }

            audioExecutor.shutdown();
            audioExecutor.awaitTermination(10, TimeUnit.SECONDS);
            audioExecutor.close();

            renderingExecutor.shutdown();
            renderingExecutor.awaitTermination(10, TimeUnit.SECONDS);
            renderingExecutor.close();

            imageExecutor.shutdown();
            imageExecutor.awaitTermination(10, TimeUnit.SECONDS);
            imageExecutor.close();

            if (audioLine != null) {
                audioLine.stop();
            }

            grabber.stop();
            grabber.release();
        } catch (FrameGrabber.Exception | LineUnavailableException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        ConsoleUtils.resetCursorPosition();

        running = false;
        paused = false;
    }

    private void createThread() {
        if (running) {
            throw new IllegalStateException("This getVideos player is already running!");
        }
        running = true;
        thread = Thread.startVirtualThread(this::run);
    }

    public void start() {
        if (running) {
            throw new IllegalStateException("This getVideos player is already running!");
        }
        createThread();
    }

    public void awaitFinish() throws InterruptedException {
        if (thread == null || !running) {
            throw new IllegalStateException("The player isn't running!");
        }
        thread.join();
    }

}
