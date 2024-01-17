package me.dynmie.monolizer.extractor.impl;

import me.dynmie.monolizer.extractor.Extractor;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.VideoSize;

import java.io.File;

/**
 * @author dynmie
 */
public class FramesExtractor implements Extractor  {

    private final int width;
    private final int height;

    public FramesExtractor(int width, int height) {
        this.width = width; // 2x console width
        this.height = height / 2;
    }

    @Override
    public void extract(File source, File output) {
        VideoAttributes videoAttributes = new VideoAttributes()
                .setSize(new VideoSize(width, height));

        EncodingAttributes videoEncodingAttributes = new EncodingAttributes();
        videoEncodingAttributes.setVideoAttributes(videoAttributes);

        Encoder videoEncoder = new Encoder();

        try {
            videoEncoder.encode(new MultimediaObject(source), output, videoEncodingAttributes);
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        }
    }
}
