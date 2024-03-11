package me.dynmie.monolizer.utils;

import me.dynmie.monolizer.MonoMain;

/**
 * @author dynmie
 */
public class ConsoleUtils {

    public static void setCursorVisibility(boolean visibility) {
        char val = visibility ? 'h' : 'l';
        System.out.printf("\033[?25%s", val);
    }

    public static void setCursorPosition(int row, int column) {
        System.out.printf("%c[%d;%df", 0x1B, row, column);
    }

    public static void resetCursorPosition() {
        setCursorPosition(0, 0);
    }

    public static void pause() {
        System.out.print("Press enter to play. ");
        MonoMain.SCANNER.nextLine();
    }

}
