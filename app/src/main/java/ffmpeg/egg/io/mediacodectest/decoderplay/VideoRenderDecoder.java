package ffmpeg.egg.io.mediacodectest.decoderplay;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/3/21.
 */

public class VideoRenderDecoder extends BaseDecoder {
    final int TIMEOUT_USEC = 10000;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private long mPrevMonoUsec = 0L;
    private long mPrevPresentUsec = 0L;
    public VideoRenderDecoder(MediaFormat paramMediaFormat, Surface paramSurface, StageDoneCallback callback) {
        super(paramMediaFormat, paramSurface, callback);
    }
    public boolean shouldNew() {
        if(mBufferInfo == null){
            return false;
        }
        if (this.mPrevMonoUsec == 0) {
            this.mPrevMonoUsec = System.nanoTime() / 1000;
            this.mPrevPresentUsec = mBufferInfo.presentationTimeUs;
            return false;
        }

        long j = mBufferInfo.presentationTimeUs - this.mPrevPresentUsec;
        //两帧之间时间间隔与真实时间差相差大于100us则等待
        if (System.nanoTime() / 1000 - this.mPrevMonoUsec <= j - 100) {
            return false;
        }
        this.mPrevMonoUsec += j;
        this.mPrevPresentUsec += j;
        return true;
    }
    public void addImediatelly(){
        if(mBufferInfo.size>0) {
            mDecoder.releaseOutputBuffer(mFrameToProcess, true);
        }else{
            mDecoder.releaseOutputBuffer(mFrameToProcess,false);
        }
    }

    @Override
    public void processFrame() {
        if (mFrameToProcess == -1) {
            getFrameFromDecoder();
        }
        outputFrame();
    }

    @Override
    public void getFrameFromDecoder() {
        int decoderStatus = mDecoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            Log.d("mytest", "no output from decoder available");
        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not important for us, since we're using Surface
            Log.d("mytest", "decoder output buffers changed");
        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat newFormat = mDecoder.getOutputFormat();
            Log.d("mytest", "decoder output format changed: " + newFormat);
        } else if (decoderStatus < 0) {
            Log.d("mytest", "decoder exception" + decoderStatus);
            throw new RuntimeException(
                    "unexpected result from decoder.dequeueOutputBuffer: " +
                            decoderStatus);
        } else { // decoderStatus >= 0
            mFrameToProcess = decoderStatus;
        }
    }

    @Override
    public void outputFrame() {
        if(!shouldNew()){
            return;
        }
        if(mFrameToProcess == -1){
            return;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d("mytest", "video output EOS");
            stageComplete();
            return;
        }
        if(mBufferInfo.size>0) {
            mDecoder.releaseOutputBuffer(mFrameToProcess, true);
        }else{
            mDecoder.releaseOutputBuffer(mFrameToProcess,false);
        }
        mFrameToProcess = -1;
    }
    public void restart(){
        mDecoder.flush();
        mPrevMonoUsec = 0L;
    }
}
