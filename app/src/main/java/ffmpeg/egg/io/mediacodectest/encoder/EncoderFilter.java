package ffmpeg.egg.io.mediacodectest.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.decoder.DecoderFilter;
import ffmpeg.egg.io.mediacodectest.extractor.ExtractorToDecoder;
import ffmpeg.egg.io.mediacodectest.openglbase.InputSurface;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

import static android.R.attr.format;

/**
 * Created by zhulinping on 17/2/9.
 */

public class EncoderFilter {
    private int testState = 0;
    private MediaCodec mAudioEncoder;
    private MediaCodec mVideoEncoder;
    private InputSurface mInputSurface;
    private DoneCallback mCallback;
    final int TIMEOUT_USEC = 10000;
    MediaMuxer mMediaMuxer = null;
    private int mAudioTrack = -1;
    private int mVideoTrack = -1;
    boolean isAudioDone = false;
    boolean isVideoDone = false;
    boolean isMuxerStarted = false;
    MediaFormat mAudioFormat;
    MediaFormat mVudioFormat;

    public EncoderFilter(MediaFormat audioFormate,MediaFormat videoFormate,DoneCallback callback) {
        mCallback = callback;
        mAudioFormat = audioFormate;
        mVudioFormat = videoFormate;
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
            mAudioEncoder = MediaCodec.createEncoderByType(mAudioFormat.getString(MediaFormat.KEY_MIME));
            mAudioEncoder.configure(mAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioEncoder.start();

            mVideoEncoder = MediaCodec.createEncoderByType(mVudioFormat.getString(MediaFormat.KEY_MIME));
            mVideoEncoder.configure(mVudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = new InputSurface(mVideoEncoder.createInputSurface());
            mInputSurface.makeCurrent();
            mVideoEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void audioEncoder() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mAudioEncoder.getOutputBuffers();
        int encoderStatus = mAudioEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        Log.d("mytest", "audio encoder");
        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            //Log.d("mytest", "no output available, spinning to await EOS");
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
                mCallback.audioDone();
            }
        }
    }

    public void videoEncoder() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mVideoEncoder.getOutputBuffers();
        int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        Log.d("mytest", "video encoder");
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
                mCallback.videoDone();
            }
        }
    }

    public void releaseAudio() {
        mAudioEncoder.stop();
        mAudioEncoder.release();
        releaseMuxer();
    }

    public void releaseVideo() {
        mVideoEncoder.stop();
        mVideoEncoder.release();
        releaseMuxer();
    }

    public void releaseMuxer() {
        switch (testState) {
            case 0:
                if (isAudioDone && isVideoDone) {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                    if (mInputSurface != null) {
                        mInputSurface.release();
                        mInputSurface = null;
                    }
                }
                break;
            case 1:
                if (isAudioDone) {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                }
                break;
            case 2:
                if (isVideoDone) {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                    if (mInputSurface != null) {
                        mInputSurface.release();
                        mInputSurface = null;
                    }
                }
                break;
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

    public void setTestState(int state) {
        testState = state;
    }

}
