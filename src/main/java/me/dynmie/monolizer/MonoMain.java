package me.dynmie.monolizer;

import me.dynmie.monolizer.player.VideoPlayer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author dynmie
 */
public class MonoMain {

    public static final File FOLDER = new File("monolizer/");
    public static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        Terminal terminal = TerminalBuilder.terminal();
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();

        boolean manualResolution = false;
        boolean color = false;
        int width = terminal.getWidth();
        int height = terminal.getHeight();
        File sourceFile = new File(FOLDER + "/source.mp4");

        for (String arg : args) {
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
                manualResolution = true;
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
                         -l        Enable video colors. (SLOW)
                         -h        Show this help menu.
                         -r        Set the frame generation resolution.
                         -s        Set the source file. If not set, the default source file will be used.""");
                return;
            }
        }

        System.out.println("Using source file: " + sourceFile);
        System.out.println("Colors: " + (color ? "Enabled" : "Disabled"));
        System.out.println("Manual Resolution: " + (manualResolution ? "Enabled" : "Disabled"));
        System.out.println("Current Resolution: " + width + "x" + height);

        VideoPlayer player = new VideoPlayer(terminal.output(), sourceFile, width, height, manualResolution, color);

        if (!manualResolution) {
            Terminal.SignalHandler signalHandler = signal -> {
                if (signal == Terminal.Signal.WINCH) {
                    player.setResolution(terminal.getWidth(), terminal.getHeight());
                }
            };

            terminal.handle(Terminal.Signal.WINCH, signalHandler);
        }

        player.start();
        try {
            player.awaitFinish();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        terminal.close();
    }

}
