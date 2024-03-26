package me.dynmie.monolizer;

import me.dynmie.monolizer.player.Asciifier;
import me.dynmie.monolizer.player.VideoPlayer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author dynmie
 */
public class MonoMain {

    public static void main(String[] args) throws IOException {
        Terminal terminal = TerminalBuilder.terminal();

        File sourceFile = new File("source.mp4");

        if (args.length > 0) {
            String path = String.join(" ", args);
            sourceFile = new File(path);
        }

        if (!sourceFile.exists()) {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter source file: ");
            String path = scanner.nextLine();

            if (path.isEmpty()) {
                System.out.println("That video doesn't exist!");
                return;
            }
            sourceFile = new File(path);
        }

        if (!sourceFile.exists()) {
            System.out.println("That video doesn't exist!");
            return;
        }

        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();

        int width = terminal.getWidth();
        int height = terminal.getHeight();

        NonBlockingReader reader = terminal.reader();

        VideoPlayer player = new VideoPlayer(System.out, sourceFile, width, height, new Asciifier(
                false,
                false,
                true,
                Asciifier.DEFAULT_BRIGHTNESS_LEVELS
        ));

        terminal.handle(Terminal.Signal.WINCH, signal -> {
            if (signal == Terminal.Signal.WINCH) {
                player.setResolution(terminal.getWidth(), terminal.getHeight());
            }
        });

        player.start();

        while (player.isRunning()) {
            int read = reader.read();
            char character = (char) read;

            switch (character) {
                case 'c' -> {
                    Asciifier old = player.getAsciifier();
                    player.setAsciifier(new Asciifier(
                            !old.isColor(),
                            old.isFullPixel(),
                            old.isTextDithering(),
                            colorThen(!old.isColor())
                    ));
                }
                case 'd' -> {
                    Asciifier old = player.getAsciifier();
                    player.setAsciifier(new Asciifier(
                            old.isColor(),
                            old.isFullPixel(),
                            !old.isTextDithering(),
                            colorThen(old.isColor())
                    ));
                }
                case 'f' -> {
                    Asciifier old = player.getAsciifier();
                    player.setAsciifier(new Asciifier(
                            old.isColor(),
                            !old.isFullPixel(),
                            old.isTextDithering(),
                            colorThen(old.isColor())
                    ));
                }
                case ' ' -> {
                    if (player.isPaused()) {
                        player.play();
                    } else {
                        player.pause();
                    }
                }
                case 'q' -> player.stop();
            }
        }

        terminal.close();
    }

    private static char[] colorThen(boolean color) {
        return color ? Asciifier.DEFAULT_COLOR_BRIGHTNESS_LEVELS : Asciifier.DEFAULT_BRIGHTNESS_LEVELS;
    }

}
