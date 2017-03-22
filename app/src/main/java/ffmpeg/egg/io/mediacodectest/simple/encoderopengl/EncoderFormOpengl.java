package ffmpeg.egg.io.mediacodectest.simple.encoderopengl;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.simple.ExtractorToDecoder;
import ffmpeg.egg.io.mediacodectest.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

/**
 * Created by zhulinping on 17/2/8.
 */

public class EncoderFormOpengl {
    private int testState = 0;
    private MediaCodec mAudioEncoder;
    private MediaCodec mVideoEncoder;
    private InputSurface mInputSurface;
    private DoneCallback mCallback;
    private ExtractorToDecoder mExtractor;
    private DecoderToOpengl mDecoder;
    final int TIMEOUT_USEC = 10000;
    MediaMuxer mMediaMuxer = null;
    private int mAudioTrack = -1;
    private int mVideoTrack = -1;
    boolean isAudioDone = false;
    boolean isVideoDone = false;
    boolean isMuxerStarted = false;

    public EncoderFormOpengl(ExtractorToDecoder extractor, DoneCallback callback) {
        mCallback = callback;
        mExtractor = extractor;
        String outputVideoPath = Environment.getExternalStorageDirectory().toString()
                + "/dcim/camera/" + System.currentTimeMillis() + ".mp4";
        try {
            mMediaMuxer = new MediaMuxer(outputVideoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initEncoder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initEncoder() {
        try {
            MediaFormat audioFormat = getAudioMediaFormate(mExtractor.getmAudioFormat());
            MediaCodecInfo audioCodecInfo = selectAudioCodec(audioFormat.getString(MediaFormat.KEY_MIME));
            if (audioCodecInfo == null) {
                Log.d("mytest", "Unable to find an appropriate codec for " + audioFormat.getString(MediaFormat.KEY_MIME));
                return;
            }
            mAudioEncoder = MediaCodec.createEncoderByType(audioFormat.getString(MediaFormat.KEY_MIME));
            mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioEncoder.start();

            MediaFormat videoFormat = getVideoFormate();
            final MediaCodecInfo videoCodecInfo = selectVideoCodec(videoFormat.getString(MediaFormat.KEY_MIME));
            if (videoCodecInfo == null) {
                Log.d("mytest", "Unable to find an appropriate codec for " + videoFormat.getString(MediaFormat.KEY_MIME));
                return;
            }
            mVideoEncoder = MediaCodec.createEncoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
            mVideoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = new InputSurface(mVideoEncoder.createInputSurface());
            mInputSurface.makeCurrent();
            mVideoEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaFormat getAudioMediaFormate(MediaFormat format) {
        MediaFormat paramString = MediaFormat.createAudioFormat("audio/mp4a-latm",
                format.getInteger("sample-rate"),
                format.getInteger("channel-count"));
        paramString.setInteger("bitrate", 57344);
        return paramString;
    }

    private MediaFormat getVideoFormate() {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", 1280, 720);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1300000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        return format;
    }

    public void audioEncoder() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mAudioEncoder.getOutputBuffers();
        int encoderStatus = mAudioEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        Log.d("mytest", "audio encoder"+encoderStatus);
        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            Log.d("mytest", " audio no output available, spinning to await EOS");
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not expected for an encoder
            outputBuffers = mAudioEncoder.getOutputBuffers();
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // should happen before receiving buffers, and should only happen once
            MediaFormat newFormat = mAudioEncoder.getOutputFormat();
            mAudioTrack = mMediaMuxer.addTrack(newFormat);
            startMuxer();
            // now that we have the Magic Goodies, start the muxer
        } else {
            if (!isMuxerStarted) {
                return;
            }
            Log.d("mytest", "audio start");
            ByteBuffer encodedData = outputBuffers[encoderStatus].duplicate();
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                // The codec config data was pulled out and fed to the muxer when we got
                // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                mBufferInfo.size = 0;
            }

            if (mBufferInfo.size != 0) {
                // adjust the ByteBuffer values to match BufferInfo (not needed?)
                encodedData.position(mBufferInfo.offset);
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                mMediaMuxer.writeSampleData(mAudioTrack, encodedData, mBufferInfo);
            }
            mAudioEncoder.releaseOutputBuffer(encoderStatus, false);
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                isAudioDone = true;
                releaseAudio();
            }
        }
    }

    public void videoEncoder() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mVideoEncoder.getOutputBuffers();
        int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            Log.d("mytest", "no output available, spinning to await EOS");
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not expected for an encoder
            outputBuffers = mVideoEncoder.getOutputBuffers();
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // should happen before receiving buffers, and should only happen once
            MediaFormat newFormat = mVideoEncoder.getOutputFormat();
            mVideoTrack = mMediaMuxer.addTrack(newFormat);
            startMuxer();
            // now that we have the Magic Goodies, start the muxer
        } else {
            if (!isMuxerStarted) {
                return;
            }
            ByteBuffer encodedData = outputBuffers[encoderStatus];
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                // The codec config data was pulled out and fed to the muxer when we got
                // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                mBufferInfo.size = 0;
            }

            if (mBufferInfo.size != 0) {
                // adjust the ByteBuffer values to match BufferInfo (not needed?)
                encodedData.position(mBufferInfo.offset);
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                mMediaMuxer.writeSampleData(mVideoTrack, encodedData, mBufferInfo);
            }
            mVideoEncoder.releaseOutputBuffer(encoderStatus, false);
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                isVideoDone = true;
                releaseVideo();
            }
        }
    }

    public void releaseAudio() {
        mCallback.audioDone();
        mDecoder.releaseAudio();
        mAudioEncoder.stop();
        mAudioEncoder.release();
        mVideoEncoder = null;
        releaseMuxer();
    }

    public void releaseVideo() {
        Log.d("mytest", "release video");
        mCallback.videoDone();
        mDecoder.releaseVideo();
        mVideoEncoder.stop();
        mVideoEncoder.release();
        mVideoEncoder = null;
        releaseMuxer();
    }

    public void releaseMuxer() {
        if (isAudioDone && isVideoDone) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            isMuxerStarted = false;
            if (mInputSurface != null) {
                mInputSurface.release();
                mInputSurface = null;
            }
        }
    }

    public void startMuxer() {
        switch (testState) {
            case 0:
                if (mAudioTrack != -1 && mVideoTrack != -1) {
                    Log.d("mytest", "muxer start");
                    mMediaMuxer.start();
                    isMuxerStarted = true;
                }
                break;
            case 1:
                if (mAudioTrack != -1) {
                    mMediaMuxer.start();
                    isMuxerStarted = true;
                }
                break;
            case 2:
                if (mVideoTrack != -1) {
                    mMediaMuxer.start();
                    isMuxerStarted = true;
                }
                break;
        }
    }

    public MediaCodec getmAudioEncoder() {
        return mAudioEncoder;
    }

    public MediaCodec getmVideoEncoder() {
        return mVideoEncoder;
    }

    public InputSurface getmInputSurface() {
        return mInputSurface;
    }

    public void setmDecoder(DecoderToOpengl decoder) {
        mDecoder = decoder;
    }

    public void setTestState(int state) {
        testState = state;
    }
    private  MediaCodecInfo selectAudioCodec(final String mimeType) {

        MediaCodecInfo result = null;
        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        LOOP:	for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {	// skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (result == null) {
                        result = codecInfo;
                        break LOOP;
                    }
                }
            }
        }
        return result;
    }
    /**
     * select the first codec that match a specific MIME type
     * @param mimeType
     * @return null if no codec matched
     */
    protected static final MediaCodecInfo selectVideoCodec(final String mimeType) {
        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {	// skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    final int format = selectColorFormat(codecInfo, mimeType);
                    if (format > 0) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     * @return 0 if no colorFormat is matched
     */
    protected static final int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {
        int result = 0;
        final MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = codecInfo.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (isRecognizedViewoFormat(colorFormat)) {
                if (result == 0)
                    result = colorFormat;
                break;
            }
        }
        if (result == 0)
            Log.d("mytest", "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }

    /**
     * color formats that we can use in this class
     */
    protected static int[] recognizedFormats;
    static {
        recognizedFormats = new int[] {
        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
        	MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    private static final boolean isRecognizedViewoFormat(final int colorFormat) {
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }
}
