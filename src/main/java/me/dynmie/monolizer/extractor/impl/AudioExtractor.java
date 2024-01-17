package me.dynmie.monolizer.extractor.impl;

import me.dynmie.monolizer.extractor.Extractor;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;

/**
 * @author dynmie
 */
public class AudioExtractor implements Extractor {

    @Override
    public void extract(File source, File output) {
        AudioAttributes audioAttributes = new AudioAttributes();

        EncodingAttributes encodingAttributes = new EncodingAttributes()
                .setOutputFormat("wav")
                .setAudioAttributes(audioAttributes);

        Encoder audioEncoder = new Encoder();

        try {
            audioEncoder.encode(new MultimediaObject(source), output, encodingAttributes);
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        }
    }

}
