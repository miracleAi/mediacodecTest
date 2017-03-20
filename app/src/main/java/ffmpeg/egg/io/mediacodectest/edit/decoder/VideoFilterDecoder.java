package ffmpeg.egg.io.mediacodectest.edit.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.edit.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.edit.surface.OutputSurface;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;
import ffmpeg.egg.io.mediacodectest.edit.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.BlendFilter;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;

/**
 * Created by zhulinping on 17/2/16.
 */

public class VideoFilterDecoder {
    private MediaCodec mVideoDecoder;
    private StageDoneCallback mCallback;
    final int TIMEOUT_USEC = 10000;
    private InputSurface mInputSurface;
    private OutputSurface mOutputSurface;
    private long mPrevMonoUsec = 0L;
    private long mPrevPresentUsec = 0L;
    private int drawFramIndex = -1;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private long firstTime = -1;
    private long gifStartTime = -1;
    private long gifLastTime = -1;
    private int filterIndex = -1;
    private TranscodingResources mRecouces;
    public VideoFilterDecoder(TranscodingResources resources, MediaFormat videoFormat, StageDoneCallback callback) {
        mCallback = callback;
        mRecouces = resources;
        mOutputSurface = new OutputSurface(resources);
        String mimeVideo = videoFormat.getString(MediaFormat.KEY_MIME);
        try {
            mVideoDecoder = MediaCodec.createDecoderByType(mimeVideo);
            mVideoDecoder.configure(videoFormat, mOutputSurface.getmSurface(), null, 0);
            mVideoDecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public MediaCodec getmVideoDecoder() {
        return mVideoDecoder;
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
    public void videoDecoder() {
        if(drawFramIndex != -1){
            outputFrame();
            return;
        }
        //MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
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
            Log.d("mytest", "decoder exception" + decoderStatus);
            throw new RuntimeException(
                    "unexpected result from decoder.dequeueOutputBuffer: " +
                            decoderStatus);
        } else { // decoderStatus >= 0
            if (mBufferInfo.size != 0) {
                mVideoDecoder.releaseOutputBuffer(decoderStatus, true);
            } else {
                mVideoDecoder.releaseOutputBuffer(decoderStatus, false);
            }
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d("mytest", "video output EOS");
                mCallback.done();
                return;
            }
            drawFramIndex = decoderStatus;
            outputFrame();
        }
    }

    public void outputFrame() {
        if(!shouldNew()){
            return ;
        }
        if(firstTime < 0){
            firstTime = mBufferInfo.presentationTimeUs;
        }
        if((mBufferInfo.presentationTimeUs - firstTime)>=3*1000000){
            if(gifStartTime < 0){
                gifStartTime = mBufferInfo.presentationTimeUs;
            }
            if(gifLastTime < 0){
                gifLastTime = mBufferInfo.presentationTimeUs;
                filterIndex = (filterIndex+1)%6 + 1;
                BlendFilter filter = new BlendFilter(mRecouces.getmContext(),"filter/zshape"+filterIndex+".png");
                setFilter(filter);
            }
            if(mBufferInfo.presentationTimeUs - gifStartTime <= 3*1000000
                    && mBufferInfo.presentationTimeUs - gifLastTime >= 3*1000000/6){
                filterIndex = (filterIndex+1)%6 + 1;
                BlendFilter filter = new BlendFilter(mRecouces.getmContext(),"filter/zshape"+filterIndex+".png");
                setFilter(filter);
            }else{
                setFilter(null);
            }
        }
        mOutputSurface.drawImage();
        mInputSurface.swapBuffers();
        drawFramIndex = -1;
    }
    public void restart(){
        mVideoDecoder.flush();
        mPrevMonoUsec = 0L;
    }
    public void release(){
        if(mVideoDecoder != null){
            mVideoDecoder.stop();
            mVideoDecoder.release();
        }
    }
    public void setmInputSurface(InputSurface surface){
        mInputSurface = surface;

    }
    public void setFilter(GPUImageFilter filter){
        mOutputSurface.setFilter(filter,false);
    }
}
