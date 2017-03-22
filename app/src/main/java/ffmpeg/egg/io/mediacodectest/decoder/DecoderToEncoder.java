package ffmpeg.egg.io.mediacodectest.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.encoder.Encoder;
import ffmpeg.egg.io.mediacodectest.extractor.ExtractorToDecoder;

/**
 * Created by zhulinping on 17/2/7.
 */

public class DecoderToEncoder {
    private MediaCodec mAudioDecoder;
    private MediaCodec mVideoDecoder;
    private ExtractorToDecoder mExtractor;
    private MediaCodec mAudioEncoder;
    private MediaCodec mVideoEncoder;
    private Encoder mEncoder;
    final int TIMEOUT_USEC = 10000;

    public DecoderToEncoder(Encoder encoder, ExtractorToDecoder extractor) {
        mExtractor = extractor;
        mEncoder = encoder;
        initDecoder();
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
            mVideoDecoder.configure(videoFormat, mEncoder.getInputSurface(), null, 0);
            mVideoDecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void audioDecoder() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        int outputIndex = mAudioDecoder.dequeueOutputBuffer(mBufferInfo, 10000L);
        ByteBuffer[] outputBuffers = mAudioDecoder.getOutputBuffers();
        ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
        if (outputIndex < 0) {
            Log.d("mytest", "no output from decoder available");
            return;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            mAudioDecoder.releaseOutputBuffer(outputIndex, false);
            Log.d("mytest", "buffer config");
            return;
        }
        int index = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
        if (index == -1) {
            Log.d("AUDIODECODER", "no audio encoder input buffer");
            return;
        }
        ByteBuffer localByteBuffer = inputBuffers[index];
        int size = mBufferInfo.size;
        if (size >= 0) {
            ByteBuffer buffer = outputBuffers[outputIndex];
            buffer.limit(mBufferInfo.offset + mBufferInfo.size);
            localByteBuffer.position(0);
            localByteBuffer.put(buffer);
            mAudioEncoder.queueInputBuffer(index, 0, size, mBufferInfo.presentationTimeUs, mBufferInfo.flags);
        }
        mAudioDecoder.releaseOutputBuffer(outputIndex, false);
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            releaseAudio();
        }
    }

    public void videoDeocder() {
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
            Log.d("mytest", "decoder exception"+decoderStatus);
            throw new RuntimeException(
                    "unexpected result from decoder.dequeueOutputBuffer: " +
                            decoderStatus);
        } else { // decoderStatus >= 0
            if(mBufferInfo.size != 0){
                Log.d("mytest", "video output true");
                mVideoDecoder.releaseOutputBuffer(decoderStatus, true);
            }else{
                Log.d("mytest", "video output false");
                mVideoDecoder.releaseOutputBuffer(decoderStatus, false);
            }
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                releaseVideo();
                Log.d("mytest", "video output EOS");
            }
        }
    }

    public void releaseAudio(){
        mExtractor.releaseAudioExtractor();
        mAudioDecoder.stop();
        mAudioDecoder.release();
    }
    public void releaseVideo(){
        mExtractor.releaseVideoExtractor();
        mVideoDecoder.stop();
        mVideoDecoder.release();
    }
    public void setmAudioEncoder(MediaCodec audioEncoder) {
        mAudioEncoder = audioEncoder;
    }

    public void setmVideoEncoder(MediaCodec videoEncoder) {
        mVideoEncoder = videoEncoder;
    }
    public MediaCodec getmAudioDecoder(){
        return  mAudioDecoder;
    }
    public MediaCodec getmVideoDecoder(){
        return mVideoDecoder;
    }
}
