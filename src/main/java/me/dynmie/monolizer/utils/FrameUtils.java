package me.dynmie.monolizer.utils;

import java.awt.image.BufferedImage;
import java.util.StringJoiner;

/**
 * @author dynmie
 */
public class FrameUtils {

    public static final char[] BRIGHTNESS_LEVELS = "          `.-':_,^=;><+!rc*/z?sLTv)J7(|F{C}fI31tlu[neoZ5Yxya]2ESwqkP6h9d4VpOGbUAKXHm8RD#$Bg0MNWQ%&@@@"
            .toCharArray();

    public static String convertFrameToText(BufferedImage image, boolean color) {
        StringJoiner joiner = new StringJoiner("\n");
        for (int y = 0; y < image.getHeight(); y++) {
            StringBuilder builder = new StringBuilder();
            for (int x = 0; x < image.getWidth(); x++) {
                if (!color) {
                    float brightness = RGBUtils.getBrightness(image.getRGB(x, y));
                    char brightnessChar = BRIGHTNESS_LEVELS[(int) (brightness * (BRIGHTNESS_LEVELS.length - 1))];
                    builder.append(brightnessChar);
                } else {
                    builder.append(getRGBColoredCharacter(image.getRGB(x, y)));
                }
            }
            joiner.add(builder);
        }

        return joiner.toString();
    }

    public static String getRGBColoredCharacter(int color) {
        int red   = (color >>> 16) & 0xFF;
        int green = (color >>>  8) & 0xFF;
        int blue  = (color >>>  0) & 0xFF;

        // calc luminance in range 0.0 to 1.0; using SRGB luminance constants
        float brightness = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;

        char brightnessChar = BRIGHTNESS_LEVELS[(int) (brightness * (BRIGHTNESS_LEVELS.length - 1))];

        return "\033[38;2;%s;%s;%sm%s".formatted(red, green, blue, brightnessChar);
    }

}
