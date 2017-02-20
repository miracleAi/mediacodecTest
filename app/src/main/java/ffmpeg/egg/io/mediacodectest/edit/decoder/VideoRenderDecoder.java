package ffmpeg.egg.io.mediacodectest.edit.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import ffmpeg.egg.io.mediacodectest.edit.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.edit.surface.OutputSurface;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;
import ffmpeg.egg.io.mediacodectest.edit.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;

/**
 * Created by zhulinping on 17/2/16.
 */

public class VideoRenderDecoder extends BaseDecoder{
    final int TIMEOUT_USEC = 10000;
    private InputSurface mInputSurface;
    private OutputSurface mOutputSurface;
    private long mPrevMonoUsec = 0L;
    private long mPrevPresentUsec = 0L;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    public VideoRenderDecoder(TranscodingResources resources, MediaFormat videoFormat, StageDoneCallback callback) {
        this(videoFormat,new OutputSurface(resources),callback);
    }
    public VideoRenderDecoder(MediaFormat format,OutputSurface surface,StageDoneCallback callback){
        super(format,surface.getmSurface(),callback);
        mOutputSurface = surface;
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
        //两帧之间时间间隔与真实时间差相差大于100us则丢弃
        if (System.nanoTime() / 1000 <= (this.mPrevMonoUsec + j) - 100) {
            return false;
        }
        this.mPrevMonoUsec += j;
        this.mPrevPresentUsec += j;
        return true;
    }

    public void addImediately() {
        mOutputSurface.drawImage();
        mInputSurface.swapBuffers();
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
            if (mBufferInfo.size != 0) {
                mDecoder.releaseOutputBuffer(decoderStatus, true);
            } else {
                mDecoder.releaseOutputBuffer(decoderStatus, false);
            }
            mFrameToProcess = decoderStatus;
        }
    }

    public void outputFrame() {
        if(mFrameToProcess == -1){
            return;
        }
        if(!shouldNew()){
            return ;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d("mytest", "video output EOS");
            stageComplete();
            return;
        }
        mOutputSurface.drawImage();
        mInputSurface.swapBuffers();
        mFrameToProcess = -1;
    }
    public void restart(){
        mDecoder.flush();
        mPrevMonoUsec = 0L;
    }
    public void setmInputSurface(InputSurface surface){
        mInputSurface = surface;

    }
    public void setFilter(GPUImageFilter filter){
        mOutputSurface.setFilter(filter,false);
    }

    @Override
    public void processFrame() {
        if(mFrameToProcess == -1){
            getFrameFromDecoder();
        }
        outputFrame();
    }
}
