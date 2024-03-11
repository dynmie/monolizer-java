package me.dynmie.monolizer;

import me.dynmie.monolizer.timer.PlaybackTimer;
import me.dynmie.monolizer.utils.ConsoleUtils;
import me.dynmie.monolizer.utils.FrameUtils;
import me.dynmie.monolizer.utils.VideoResizer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author dynmie
 */
public class MonoMain {

    public static final File FOLDER = new File("monolizer/");
    public static final File TEMP_FOLDER = new File(FOLDER + File.separator + "temp");
    public static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws InterruptedException {
        boolean resize = true;
        boolean color = false;
        int width = 840;
        int height = 680;
        File sourceFile = new File(FOLDER + "/source.mp4");

        for (String arg : args) {
            if (arg.equals("-c")) {
                resize = false;
                continue;
            }
            if (arg.equals("-l")) {
                color = true;
                continue;
            }
            if (arg.startsWith("-r")) {
                String[] split = arg.replaceFirst("-r", "").split("x");
                int w;
                int h;
                try {
                    w = Integer.parseInt(split[0]);
                    h = Integer.parseInt(split[1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    System.out.println("Invalid resolution specified.");
                    return;
                }
                width = w;
                height = h;
                continue;
            }
            if (arg.startsWith("-s")) {
                String path = arg.replaceFirst("-s", "");
                sourceFile = new File(path);
                continue;
            }
            if (arg.startsWith("-h") || arg.startsWith("--help")) {
                System.out.println("""
                        Monolizer Help
                         -c        Do not resize the video.
                         -l        Enable video colors. (SLOW)
                         -h        Show this help menu.
                         -r        Set the frame generation resolution.
                         -s        Set the source file. If not set, the default source file will be used.""");
                return;
            }
        }

        System.out.println("Using source file: " + sourceFile);
        System.out.println("Colors: " + (color ? "Enabled" : "Disabled"));
        System.out.println("Resize: " + (resize ? "Enabled" : "Disabled"));
        System.out.println("Resolution: " + width + "x" + height);

        if (resize) {
            File outputFile = new File(TEMP_FOLDER + "/output.mp4");
            VideoResizer.resizeVideo(sourceFile, outputFile, width, height / 2);

            sourceFile = outputFile;
        }

        ConsoleUtils.pause();
        ConsoleUtils.setCursorVisibility(false);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(sourceFile)) {
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

            ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
            ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

            long maxReadAheadBufferMicros = TimeUnit.MILLISECONDS.toMicros(1000);

            long lastTimestamp = -1L;
            while (!Thread.interrupted()) {
                Frame frame = grabber.grab();
                if (frame == null) break;

                if (lastTimestamp == -1L) {
                    playbackTimer.init();
                }

                lastTimestamp = frame.timestamp;

                // if frame is a video frame
                try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
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
                            System.out.append(text);
                            System.out.flush();
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
        } catch (FrameGrabber.Exception | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
