package me.dynmie.monolizer;

import me.dynmie.monolizer.player.VideoPlayer;
import me.dynmie.monolizer.utils.ConsoleUtils;

import java.io.File;
import java.util.Scanner;

/**
 * @author dynmie
 */
public class MonoMain {

    public static final File FOLDER = new File("monolizer/");
    public static final File TEMP_FOLDER = new File(FOLDER + File.separator + "temp");
    public static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        boolean resize = true;
        boolean color = false;
        int width = 923;
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

        VideoPlayer player = new VideoPlayer(sourceFile, width, height, resize, color);

        ConsoleUtils.pause();

        player.start();
    }

}
