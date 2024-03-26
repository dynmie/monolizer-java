package me.dynmie.monolizer.utils;

/**
 * @author dynmie
 */
public class ConsoleUtils {

    public static void setCursorVisibility(boolean visibility) {
        char val = visibility ? 'h' : 'l';
        System.out.printf("\033[?25%s", val);
    }

    public static String getCursorPositionEscapeCode(int row, int column) {
        return "%c[%d;%df".formatted(0x1B, row, column);
    }

    public static void setCursorPosition(int row, int column) {
        System.out.print(getCursorPositionEscapeCode(row, column));
    }

    public static void resetCursorPosition() {
        setCursorPosition(1, 1);
    }

    public static String getResetCursorPositionEscapeCode() {
        return getCursorPositionEscapeCode(1, 1);
    }

    public static String getForegroundResetCode() {
        return "\033[39m";
    }

    public static String getBackgroundResetCode() {
        return "\033[49m";
    }

    public static String getForegroundColorEscapeCode(int red, int green, int blue) {
        return "\033[38;2;%s;%s;%sm".formatted(red, green, blue);
    }

    public static String getBackgroundColorEscapeCode(int red, int green, int blue) {
        return "\033[48;2;%s;%s;%sm".formatted(red, green, blue);
    }
}
