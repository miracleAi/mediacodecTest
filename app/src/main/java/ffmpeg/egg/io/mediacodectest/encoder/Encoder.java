package ffmpeg.egg.io.mediacodectest.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.extractor.ExtractorToDecoder;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

/**
 * Created by zhulinping on 17/2/7.
 */

public class Encoder {
    private static final boolean DEBUG = false;	// TODO set false on release
    private static final String TAG = "MediaVideoEncoder";

    private MediaCodec mAudioEncoder;
    private MediaCodec mVideoEncoder;
    private Surface inputSurface;
    private DoneCallback mCallback;
    private ExtractorToDecoder mExtractor;
    final int TIMEOUT_USEC = 10000;
    MediaMuxer mMediaMuxer = null;
    private int mAudioTrack = -1;
    private int mVideoTrack = -1;
    boolean isAudioDone = false;
    boolean isVideoDone = false;
    boolean isMuxerStart = false;

    public Encoder(ExtractorToDecoder extractor, DoneCallback callback) {
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
            mAudioEncoder = MediaCodec.createEncoderByType(audioFormat.getString(MediaFormat.KEY_MIME));
            mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioEncoder.start();

            MediaFormat videoFormat = getVideoFormate();
            mVideoEncoder = MediaCodec.createEncoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
            mVideoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurface = mVideoEncoder.createInputSurface();
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
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411Planar);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1300000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        return format;
    }

    public void audioEncoder() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mAudioEncoder.getOutputBuffers();
        int encoderStatus = mAudioEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            Log.d("mytest", "no output available, spinning to await EOS");
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not expected for an encoder
            outputBuffers = mAudioEncoder.getOutputBuffers();
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // should happen before receiving buffers, and should only happen once
            MediaFormat newFormat = mAudioEncoder.getOutputFormat();
            mAudioTrack = mMediaMuxer.addTrack(newFormat);
            startMediaMuxer();
            // now that we have the Magic Goodies, start the muxer
        } else {
            if(!isMuxerStart){
                Log.d("mytest","muxer not start");
                return;
            }
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
            startMediaMuxer();
            // now that we have the Magic Goodies, start the muxer
        } else {
            if(!isMuxerStart){
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

        mAudioEncoder.stop();
        mAudioEncoder.release();
        releaseMuxer();
    }

    public void releaseVideo() {
        mCallback.videoDone();

        mVideoEncoder.stop();
        mVideoEncoder.release();
        releaseMuxer();
    }

    public void releaseMuxer() {
        if (isAudioDone && isVideoDone) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            isMuxerStart = false;
        }
    }

    public MediaCodec getmAudioEncoder() {
        return mAudioEncoder;
    }

    public MediaCodec getmVideoEncoder() {
        return mVideoEncoder;
    }

    public Surface getInputSurface() {
        return inputSurface;
    }
    public void startMediaMuxer(){
        if(mAudioTrack != -1 && mVideoTrack != -1){
            Log.d("mytest","start muxer");
            mMediaMuxer.start();
            isMuxerStart = true;
        }
    }
}
