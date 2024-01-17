package me.dynmie.monolizer;

import me.dynmie.monolizer.extractor.impl.AudioExtractor;
import me.dynmie.monolizer.extractor.impl.FramesExtractor;
import me.dynmie.monolizer.player.ASCIIPlayer;
import me.dynmie.monolizer.utils.ConsoleUtils;
import me.dynmie.monolizer.utils.FrameUtils;

import java.io.File;
import java.util.Scanner;

/**
 * @author dynmie
 */
public class MonoMain {

    public static final File FOLDER = new File("monolizer/");
    public static final File TEMP_FOLDER = new File(FOLDER + File.separator + "temp");
    public static final Scanner SCANNER = new Scanner(System.in);

    public static final char[] BRIGHTNESS_LEVELS = "          `.-':_,^=;><+!rc*/z?sLTv)J7(|F{C}fI31tlu[neoZ5Yxya]2ESwqkP6h9d4VpOGbUAKXHm8RD#$Bg0MNWQ%&@@@"
            .toCharArray();

    public static void main(String[] args) throws InterruptedException {
        boolean useCache = false;
        int width = 120;
        int height = 90;

        for (String arg : args) {
            useCache = arg.equals("-c");
            if (arg.startsWith("-r")) {
                String[] wh = arg.replaceFirst("-r", "").split("x");
                try {
                    width = Integer.parseInt(wh[0]);
                    height = Integer.parseInt(wh[1]);
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    System.out.println("Invalid resolution.");
                    return;
                }
                System.out.println("Resolution set to " + width + "x" + height + ".");
            }
        }

        if (useCache) {
            System.out.println("Now using the cache.");
        }

        File sourceFile = new File(FOLDER + "/source.mp4");
        if (!sourceFile.exists()) {
            System.out.println("No source file! Double check to see if you have a source video here: " + sourceFile);
            return;
        }

        File audioFile = new File(TEMP_FOLDER + "/audio.wav");
        if (!(useCache && audioFile.exists())) {
            if (audioFile.exists()) {
                audioFile.delete();
            }
            System.out.print("Extracting audio...");
            new AudioExtractor().extract(sourceFile, audioFile);
            System.out.println(" Done!");
        } else {
            System.out.println("Attempting to use existing audio file...");
        }

        File videoTargetFile = new File(TEMP_FOLDER + "/frames/frame%0d.bmp");
        File framesFolder = videoTargetFile.getParentFile();
        if (!(useCache && framesFolder.exists())) {
            if (framesFolder.exists()) framesFolder.delete();
            System.out.print("Extracting frames...");
            new FramesExtractor(width, height).extract(sourceFile, videoTargetFile);
            System.out.println(" Done!");
        } else {
            System.out.println("Attempting to use existing frames...");
        }

        System.out.print("Converting frames to ASCII...");
        String[] frames = FrameUtils.loadFramesFromFolder(framesFolder);
        System.out.println(" Done!");

        ConsoleUtils.pause();

        ASCIIPlayer player = new ASCIIPlayer(frames, audioFile);
        player.play();

        Thread.sleep(Long.MAX_VALUE); // idk shouldnt do this but i dont care
    }

}
