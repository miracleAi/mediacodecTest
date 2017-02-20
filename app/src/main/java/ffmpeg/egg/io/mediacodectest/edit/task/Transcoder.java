package ffmpeg.egg.io.mediacodectest.edit.task;

import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.edit.decoder.AudioDecoder;
import ffmpeg.egg.io.mediacodectest.edit.decoder.VideoDecoder;
import ffmpeg.egg.io.mediacodectest.edit.encoder.AudioEncoder;
import ffmpeg.egg.io.mediacodectest.edit.encoder.VideoEncoder;
import ffmpeg.egg.io.mediacodectest.edit.extractor.AudioExtractor;
import ffmpeg.egg.io.mediacodectest.edit.extractor.VideoExtractor;
import ffmpeg.egg.io.mediacodectest.edit.muxer.Muxer;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;
import ffmpeg.egg.io.mediacodectest.edit.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;

/**
 * Created by zhulinping on 17/2/15.
 */

public class Transcoder {
    private AudioExtractor mAudioExtractor;
    private VideoExtractor mVideoExtractor;
    private AudioDecoder mAudioDecoder;
    private VideoDecoder mVideoDecoder;
    private AudioEncoder mAudioEncoder;
    private VideoEncoder mVideoEncoder;
    String mFilePath;
    private boolean mAudioEncoderDone = false;
    private boolean mVideoEncoderDone = false;
    private boolean mAudioDecoderDone = false;
    private boolean mVideoDecoderDone = false;
    private boolean mAudioExtractorDone = false;
    private boolean mVideoExtractorDone = false;
    private Muxer mMuxer;
    private boolean mIsSilence = false;
    private TranscodingResources mRecources;

    public Transcoder(TranscodingResources recources, String path,String output) {
        mFilePath = path;
        mRecources = recources;
        mMuxer = new Muxer(output);
        mAudioExtractor = new AudioExtractor(path, new AudioExtractorDone());
        mVideoExtractor = new VideoExtractor(path, new VideoExtractorDone());
        mAudioEncoder = new AudioEncoder(mMuxer, getAudioMediaFormate(), new AudioEncoderDoneCallback());
        mVideoEncoder = new VideoEncoder(mMuxer, getVideoFormate(), new VideoEncoderCallback());
        mAudioDecoder = new AudioDecoder(mAudioExtractor.getFormat(), new AudioDecodenDone());
        mVideoDecoder = new VideoDecoder(mVideoExtractor.getFormat(), recources, new VideoDeocderDone());
        mAudioExtractor.setDecoder(mAudioDecoder.getmDecoder());
        mVideoExtractor.setDecoder(mVideoDecoder.getmDecoder());
        mAudioDecoder.setmEncoder(mAudioEncoder.getCodec());
        mVideoDecoder.setmEncoder(mVideoEncoder.getCodec());
        mVideoDecoder.setmInputSurface(mVideoEncoder.getInputSurface());
    }

    public void setFilter(GPUImageFilter filter) {
        mVideoDecoder.setFilter(filter);
    }

    public void setSilence(boolean isSilence) {
        mIsSilence = isSilence;
        mMuxer.setmIsSilence(isSilence);
        if (isSilence) {
            mAudioExtractorDone = true;
            mAudioDecoderDone = true;
            mAudioEncoderDone = true;
        }
    }

    private MediaFormat getAudioMediaFormate() {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(mFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat audioFormat = null;
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                audioFormat = format;
                break;
            }
        }
        MediaFormat paramString = MediaFormat.createAudioFormat("audio/mp4a-latm",
                audioFormat.getInteger("sample-rate"),
                audioFormat.getInteger("channel-count"));
        paramString.setInteger("bitrate", 57344);
        ;
        return paramString;
    }

    private MediaFormat getVideoFormate() {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", mRecources.getVideoWidth(), mRecources.getVideoHeight());
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1300000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        return format;
    }

    public void transcoder() {
        while (!(mVideoEncoderDone && mAudioEncoderDone)) {
            if (!mAudioExtractorDone) {
                mAudioExtractor.processFrame();
            }
            if (!mAudioDecoderDone) {
                mAudioDecoder.processFrame();
            }
            if (!mAudioEncoderDone) {
                mAudioEncoder.processFrame();
            }
            if (!mVideoExtractorDone) {
                mVideoExtractor.processFrame();
            }
            if (!mVideoDecoderDone) {
                mVideoDecoder.processFrame();
            }
            if (!mVideoEncoderDone) {
                mVideoEncoder.processFrame();
            }
        }
    }

    class AudioExtractorDone implements StageDoneCallback {

        @Override
        public void done() {
            mAudioExtractorDone = true;
        }
    }

    class VideoExtractorDone implements StageDoneCallback {

        @Override
        public void done() {
            mVideoExtractorDone = true;
        }
    }

    class AudioDecodenDone implements StageDoneCallback {

        @Override
        public void done() {
            mAudioDecoderDone = true;
        }
    }

    class VideoDeocderDone implements StageDoneCallback {

        @Override
        public void done() {
            mVideoDecoderDone = true;
        }
    }

    class AudioEncoderDoneCallback implements StageDoneCallback {

        @Override
        public void done() {
            mAudioEncoderDone = true;
            mAudioEncoder.release();
            mAudioDecoder.release();
            mAudioExtractor.release();
            if (mVideoEncoderDone) {
                mMuxer.stopMuxer();
            }
        }
    }

    class VideoEncoderCallback implements StageDoneCallback {

        @Override
        public void done() {
            mVideoEncoderDone = true;
            mVideoEncoder.release();
            mVideoDecoder.release();
            mVideoExtractor.release();
            if (mAudioEncoderDone) {
                mMuxer.stopMuxer();
            }
        }
    }
}
