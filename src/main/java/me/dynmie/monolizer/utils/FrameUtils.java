package me.dynmie.monolizer.utils;

import me.dynmie.monolizer.MonoMain;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;

/**
 * @author dynmie
 */
public class FrameUtils {

    public static String[] loadFramesFromFolder(File folder) {
        String[] list = folder.list();
        if (list == null) return new String[0];

        BufferedImage[] bufferedImages = new BufferedImage[list.length];
        for (int i = 0; i < list.length; i++) {
            File file = new File(folder + File.separator + "frame" + (i+1) + ".bmp");

            try {
                bufferedImages[i] = ImageIO.read(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String[] frames = new String[bufferedImages.length];
        for (int i = 0; i < bufferedImages.length; i++) {
            BufferedImage image = bufferedImages[i];

            StringJoiner joiner = new StringJoiner("\n");
            for (int h = 0; h < image.getHeight(); h++) {
                StringBuilder builder = new StringBuilder();
                for (int w = 0; w < image.getWidth(); w++) {
                    float brightness = RGBUtils.getBrightness(image.getRGB(w, h));
                    char brightnessChar = MonoMain.BRIGHTNESS_LEVELS[(int) (brightness * (MonoMain.BRIGHTNESS_LEVELS.length - 1))];
                    builder.append(brightnessChar);
                }
                joiner.add(builder);
            }

            bufferedImages[i] = null;
            frames[i] = joiner.toString();
        }

        return frames;
    }

}
