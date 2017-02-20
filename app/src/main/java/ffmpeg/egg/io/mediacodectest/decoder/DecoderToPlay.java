package ffmpeg.egg.io.mediacodectest.decoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.extractor.ExtractorToDecoder;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

/**
 * Created by zhulinping on 17/2/7.
 */

public class DecoderToPlay {
    private MediaCodec mAudioDecoder;
    private MediaCodec mVideoDecoder;
    private ExtractorToDecoder mExtractor;
    private Surface mSurface;
    private DoneCallback mCallback;
    private boolean isAudioDecoderDone = false;
    private boolean isVideoDecoderDone = false;
    private AudioTrack mAudioTrack;
    final int TIMEOUT_USEC = 10000;

    public DecoderToPlay(Surface surface, ExtractorToDecoder extractor, DoneCallback callback) {
        mSurface = surface;
        mExtractor = extractor;
        mCallback = callback;
        initDecoder();
        initAudioTrack();
    }

    private void initAudioTrack() {
        int j = mExtractor.getmAudioFormat().getInteger("sample-rate");
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, j, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, j, AudioTrack.MODE_STREAM);
        mAudioTrack.setPlaybackRate(j);
        mAudioTrack.play();
    }

    private void initDecoder() {
        try {
            MediaFormat audioFormat = mExtractor.getmAudioFormat();
            String mimeAudio = audioFormat.getString(MediaFormat.KEY_MIME);
            mAudioDecoder = MediaCodec.createDecoderByType(mimeAudio);
            mAudioDecoder.configure(audioFormat, null, null, 0);
            mAudioDecoder.start();

            MediaFormat videoFormat = mExtractor.getmVideoFormat();
            String mimeVideo = videoFormat.getString(MediaFormat.KEY_MIME);
            mVideoDecoder = MediaCodec.createDecoderByType(mimeVideo);
            mVideoDecoder.configure(videoFormat, mSurface, null, 0);
            mVideoDecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaCodec getmAudioDecoder() {
        return mAudioDecoder;
    }

    public MediaCodec getmVideoDecoder() {
        return mVideoDecoder;
    }

    public void videoDecoder() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        int decoderStatus = mVideoDecoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            Log.d("mytest", "no output from decoder available");
        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not important for us, since we're using Surface
            Log.d("mytest", "decoder output buffers changed");
        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat newFormat = mVideoDecoder.getOutputFormat();
            Log.d("mytest", "decoder output format changed: " + newFormat);
        } else if (decoderStatus < 0) {
            throw new RuntimeException(
                    "unexpected result from decoder.dequeueOutputBuffer: " +
                            decoderStatus);
        } else { // decoderStatus >= 0
            if(mBufferInfo.size>0) {
                mVideoDecoder.releaseOutputBuffer(decoderStatus, true);
            }else{
                mVideoDecoder.releaseOutputBuffer(decoderStatus,false);
            }
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                isVideoDecoderDone = true;
                releaseVideo();
                Log.d("mytest", "output EOS");
            }
        }
    }

    public void releaseAudio() {
        mCallback.audioDone();
        mAudioDecoder.stop();
        mAudioDecoder.release();
        mExtractor.releaseAudioExtractor();
    }

    private void releaseVideo() {
        mCallback.videoDone();
        mVideoDecoder.stop();
        mVideoDecoder.release();
        mExtractor.releaseVideoExtractor();
    }

    public void audioDecoder() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        int outputIndex = mAudioDecoder.dequeueOutputBuffer(mBufferInfo, 10000L);
        ByteBuffer[] decoderoutputBuffers = mAudioDecoder.getOutputBuffers();
        if(outputIndex < 0){
            return;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            mAudioDecoder.releaseOutputBuffer(outputIndex, false);
            return;
        }
        int size = mBufferInfo.size;
        if (size >= 0) {
            ByteBuffer buffer = decoderoutputBuffers[outputIndex].duplicate();
            buffer.position(mBufferInfo.offset);
            buffer.limit(mBufferInfo.offset + mBufferInfo.size);
            ByteBuffer localByteBuffer = buffer;
            byte[] arrayOfByte = new byte[size];
            localByteBuffer.get(arrayOfByte);
            localByteBuffer.clear();
            mAudioTrack.write(arrayOfByte, 0, size);
        }
        mAudioDecoder.releaseOutputBuffer(outputIndex, false);
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            isAudioDecoderDone = true;
            releaseAudio();
        }
    }
}
