package me.dynmie.monolizer.utils;

/**
 * @author dynmie
 */
public class RGBUtils {

    // stackoverflow code
    public static float getBrightness(int color) {
        // extract each color component
        int red   = (color >>> 16) & 0xFF;
        int green = (color >>>  8) & 0xFF;
        int blue  = (color >>>  0) & 0xFF;

        // calc luminance in range 0.0 to 1.0; using SRGB luminance constants
        return (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
    }

}
