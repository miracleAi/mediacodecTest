package ffmpeg.egg.io.mediacodectest.decoderplay;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/3/22.
 */

public class AudioDecoder extends BaseDecoder{
    private MediaCodec.BufferInfo mBufferInfo;
    private MediaCodec mEncoder;
    public AudioDecoder(MediaFormat paramMediaFormat, Surface paramSurface, StageDoneCallback callback) {
        super(paramMediaFormat, paramSurface, callback);
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
        if(mDecoder == null){
            return;
            //throw new NullPointerException("audio decoder null");
        }
        mBufferInfo = new MediaCodec.BufferInfo();
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
        if(mEncoder == null){
            return;
        }
        ByteBuffer[] outputBuffers = mDecoder.getOutputBuffers();
        ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        int inputIndex = mEncoder.dequeueInputBuffer(10000);
        if (inputIndex == -1) {
            Log.d("AUDIODECODER", "no audio encoder input buffer");
            return;
        }
        ByteBuffer localByteBuffer = inputBuffers[inputIndex];
        int size = mBufferInfo.size;
        if (size >= 0) {
            ByteBuffer buffer = outputBuffers[mFrameToProcess].duplicate();
            buffer.position(mBufferInfo.offset);
            buffer.limit(mBufferInfo.offset + mBufferInfo.size);
            localByteBuffer.position(0);
            localByteBuffer.put(buffer);
            mEncoder.queueInputBuffer(inputIndex, 0, size, mBufferInfo.presentationTimeUs, mBufferInfo.flags);
        }
        mDecoder.releaseOutputBuffer(mFrameToProcess, false);
        mFrameToProcess = -1;
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            stageComplete();
        }
    }
    public void setmEncoder(MediaCodec encoder){
        mEncoder = encoder;
    }
}
