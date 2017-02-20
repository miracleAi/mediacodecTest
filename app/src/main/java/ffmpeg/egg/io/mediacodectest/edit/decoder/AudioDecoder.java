package ffmpeg.egg.io.mediacodectest.edit.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/2/15.
 */

public class AudioDecoder extends BaseDecoder{
    private MediaCodec mEncoder;
    MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    public AudioDecoder(MediaFormat paramMediaFormat, StageDoneCallback callback) {
        super(paramMediaFormat, null, callback);
    }

    @Override
    public void processFrame() {
        if(mFrameToProcess == -1){
            getFrameFromDecoder();
        }
        outputFrame();
    }

    @Override
    public void getFrameFromDecoder() {
        int outputIndex = mDecoder.dequeueOutputBuffer(mBufferInfo, 10000L);
        if (outputIndex < 0) {
            return;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            mDecoder.releaseOutputBuffer(outputIndex, false);
            return;
        }
        mFrameToProcess = outputIndex;
    }

    @Override
    public void outputFrame() {
        if(mFrameToProcess == -1){
            return;
        }
        ByteBuffer[] outputBuffers = mDecoder.getOutputBuffers();
        ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        int index = mEncoder.dequeueInputBuffer(10000L);
        if (index == -1) {
            Log.d("AUDIODECODER", "no audio encoder input buffer");
            return;
        }
        ByteBuffer localByteBuffer = inputBuffers[index];
        int size = mBufferInfo.size;
        if (size >= 0) {
            ByteBuffer buffer = outputBuffers[mFrameToProcess];
            localByteBuffer.position(0);
            localByteBuffer.put(buffer);
            mEncoder.queueInputBuffer(index, 0, size, mBufferInfo.presentationTimeUs, mBufferInfo.flags);
        }
        mDecoder.releaseOutputBuffer(mFrameToProcess, false);
        mFrameToProcess = -1;
    }
    public void setmEncoder(MediaCodec encoder){
        mEncoder = encoder;
    }
}