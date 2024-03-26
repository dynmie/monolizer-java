package me.dynmie.monolizer.player;

import me.dynmie.monolizer.utils.ConsoleUtils;
import me.dynmie.monolizer.utils.RGBUtils;

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

/**
 * @author dynmie
 */
public class Asciifier {
    private static final float DITHER_FACTOR = 0.0625f;
    private static final int DITHER_NEIGHBOR_RIGHT_FACTOR = 7;
    private static final int DITHER_NEIGHBOR_BOTTOM_LEFT_FACTOR = 3;
    private static final int DITHER_NEIGHBOR_BOTTOM_FACTOR = 5;
    private static final int DITHER_NEIGHBOR_BOTTOM_RIGHT_FACTOR = 1;
    private static final int COLOR_BATCHING_THRESHOLD = 2;

    //    public static final char[] DEFAULT_BRIGHTNESS_LEVELS = " .-+*wGHM#&%".toCharArray();
    public static final char[] DEFAULT_COLOR_BRIGHTNESS_LEVELS = " `.-':_,^=;><+!rc*/z?sLTv)J7(|Fi{C}fI31tlu[neoZ5Yxjya]2ESwqkP6h9d4VpOGbUAKXHm8RD#$Bg0MNWQ%&@"
            .toCharArray();
    public static final char[] DEFAULT_BRIGHTNESS_LEVELS = " .:-=+*#%@".toCharArray();

    private final boolean color;
    private final boolean fullPixel;
    private final boolean textDithering;
    private final char[] brightnessLevels;

    public Asciifier(boolean color, boolean fullPixel, boolean textDithering, char[] brightnessLevels) {
        this.color = color;
        this.fullPixel = fullPixel;
        this.textDithering = textDithering;
        this.brightnessLevels = brightnessLevels;
    }

    public String createFrame(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        float[][] textDitheringErrors = new float[width][height];

        String[] lines = new String[height];
        IntStream.range(0, height)
                .forEach(y -> {
                    StringBuilder builder = new StringBuilder();
                    int prevColor = -1;

                    for (int x = 0; x < width; x++) {
                        int currentColor = image.getRGB(x, y);

                        boolean almostSameColor = isAlmostSameColor(currentColor, prevColor);

                        String pixel = createPixel(width, height, x, y, currentColor, almostSameColor, textDitheringErrors);
                        builder.append(pixel);

                        if (!almostSameColor) {
                            prevColor = currentColor;
                        }
                    }

                    lines[y] = builder.toString();
                });
        return String.join("\n", lines);
    }

    private String createPixel(int width, int height, int x, int y, int currentColor, boolean almostSameColor, float[][] textDitheringErrors) {
        int red   = (currentColor >>> 16) & 0xFF;
        int green = (currentColor >>>  8) & 0xFF;
        int blue  = (currentColor       ) & 0xFF;

        float brightness = RGBUtils.getBrightness(red, green, blue); // percentage

        if (textDithering && !(fullPixel && color)) {
            float thisError = textDitheringErrors[x][y];

            brightness += thisError;

            float perceivedBrightness = (float) indexFromBrightness(brightness) / (brightnessLevels.length - 1);
            float error = (brightness - perceivedBrightness) * DITHER_FACTOR;

            writeDitheringError(width, height, x, y, error, textDitheringErrors);

            brightness = Math.clamp(brightness, 0, 1); // min 0, max 1
        }

        if (color) {
            // some optimizations
            if (x == 0 || !almostSameColor) {
//                                if (!fullPixel) {
//                                    int maxComponent = Math.max(red, Math.max(green, blue));
//
//                                    // Ensure that the maximum component remains at its original value
//                                    double scaleFactor = 255.0 / maxComponent;
//
//                                    // Scale down all components proportionally
//                                    red = (int) (red * scaleFactor);
//                                    green = (int) (green * scaleFactor);
//                                    blue = (int) (blue * scaleFactor);
//                                }

                char brightnessChar = getRGBBrightnessCharFromColor(brightness);

                String ret;
                if (brightnessChar == ' ' && !fullPixel) {
                    ret =  " ";
                } else if (fullPixel) {
                    ret = ConsoleUtils.getBackgroundColorEscapeCode(red, green, blue) + brightnessChar;
                } else {
                    ret = ConsoleUtils.getForegroundColorEscapeCode(red, green, blue) + brightnessChar;
                }

                return ret;
            } else {
                return getRGBBrightnessCharFromColor(brightness) + "";
            }
        } else {
            return getBrightnessCharFromColor(brightness) + "";
        }
    }

    private int indexFromBrightness(float brightness) {
        return (int) (brightness * (brightnessLevels.length - 1));
    }

    private char getBrightnessCharFromColor(float brightness) {
        return brightnessLevels[indexFromBrightness(brightness)];
    }

    private char getRGBBrightnessCharFromColor(float brightness) {
        if (fullPixel) {
            return ' ';
        }
        return brightnessLevels[indexFromBrightness(brightness)];
    }

    public boolean isColor() {
        return color;
    }

    public boolean isFullPixel() {
        return fullPixel;
    }

    public boolean isTextDithering() {
        return textDithering;
    }

    public char[] getBrightnessLevels() {
        return brightnessLevels;
    }

    private static boolean isAlmostSameColor(int first, int second) {
        int red1 = (first >> 16) & 0xff;
        int green1 = (first >> 8) & 0xff;
        int blue1 = first & 0xff;

        int red2 = (second >> 16) & 0xff;
        int green2 = (second >> 8) & 0xff;
        int blue2 = second & 0xff;

        int redDiff = Math.abs(red1 - red2);
        int greenDiff = Math.abs(green1 - green2);
        int blueDiff = Math.abs(blue1 - blue2);

        // Calculate the color distance
        double distance = Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);

        // Check if it's within the threshold
        return distance < COLOR_BATCHING_THRESHOLD;
    }

    private static void writeDitheringError(int width, int height, int x, int y, float error, float[][] errors) {
        if (x < width - 1) {
            errors[x + 1][y] += error * DITHER_NEIGHBOR_RIGHT_FACTOR;
        }

        if (y < height - 1) {
            if (x > 0) {
                errors[x - 1][y + 1] += error * DITHER_NEIGHBOR_BOTTOM_LEFT_FACTOR;
            }
            errors[x][y + 1] += error * DITHER_NEIGHBOR_BOTTOM_FACTOR;
            if (x < width - 1) {
                errors[x + 1][y + 1] += error * DITHER_NEIGHBOR_BOTTOM_RIGHT_FACTOR;
            }
        }
    }

}
