package me.dynmie.monolizer.utils;

/**
 * @author dynmie
 */
public class RGBUtils {

    public static float getBrightness(int red, int green, int blue) {
        // calc luminance in range 0.0 to 1.0; using SRGB luminance constants
        return (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
    }

}
