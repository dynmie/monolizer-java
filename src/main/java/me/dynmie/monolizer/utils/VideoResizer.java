//package me.dynmie.monolizer.utils;
//
//import org.bytedeco.ffmpeg.global.avutil;
//import org.bytedeco.javacv.*;
//
//import java.io.File;
//
///**
// * @author dynmie
// */
//public class VideoResizer {
//
//    public static void resizeVideo(File inputFile, File outputFile, int width, int height) {
//        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile); FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, width, height)) {
//            grabber.start();
//
//            recorder.setVideoCodec(grabber.getVideoCodec());
//            recorder.setFormat(grabber.getFormat());
//            recorder.setFrameRate(grabber.getFrameRate());
//            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//
//            recorder.setAudioChannels(grabber.getAudioChannels());
//            recorder.setSampleRate(grabber.getSampleRate());
//            recorder.setAudioChannels(grabber.getAudioChannels());
//            recorder.setAudioBitrate(grabber.getAudioBitrate());
//
//            recorder.start();
//
//            try (FFmpegFrameFilter filter = new FFmpegFrameFilter("scale=" + width + ":" + height, grabber.getImageWidth(), grabber.getImageHeight())) {
//                filter.start();
//
//                Frame frame;
//                while ((frame = grabber.grabFrame()) != null) {
//                    filter.push(frame);
//                    Frame resizedFrame = filter.pull();
//                    if (resizedFrame != null) {
//                        recorder.record(resizedFrame);
//                    }
//                }
//
//                filter.stop();
//            }
//
//            grabber.stop();
//            recorder.stop();
//        } catch (FrameGrabber.Exception | FrameRecorder.Exception | FrameFilter.Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
