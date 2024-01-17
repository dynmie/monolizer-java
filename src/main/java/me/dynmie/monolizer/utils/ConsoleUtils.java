package me.dynmie.monolizer.utils;

import me.dynmie.monolizer.MonoMain;

/**
 * @author dynmie
 */
public class ConsoleUtils {

    public static final char ESC_CODE = 0x1B;

    public static void setCursorPosition(int row, int column) {
        System.out.printf("%c[%d;%df", ESC_CODE, row, column);
    }

    public static void resetCursorPosition() {
        setCursorPosition(0, 0);
    }

    public static void pause() {
        System.out.print("Press enter to play. ");
        MonoMain.SCANNER.nextLine();
    }

}
