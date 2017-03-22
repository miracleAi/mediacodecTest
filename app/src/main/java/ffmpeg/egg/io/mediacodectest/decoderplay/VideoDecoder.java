package ffmpeg.egg.io.mediacodectest.decoderplay;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import ffmpeg.egg.io.mediacodectest.surface.OutputSurface;
import ffmpeg.egg.io.mediacodectest.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;
import ffmpeg.egg.io.mediacodectest.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/3/22.
 */

public class VideoDecoder extends BaseDecoder{
    private MediaCodec mEncoder;
    private OutputSurface mOutputSurface;
    protected InputSurface mInputSurface;
    MediaCodec.BufferInfo mBufferInfo;
    public VideoDecoder(MediaFormat formate, TranscodingResources recources,StageDoneCallback callback){
        this(formate,new OutputSurface(recources),callback);
    }
    public VideoDecoder(MediaFormat paramMediaFormat, OutputSurface paramSurface, StageDoneCallback callback) {
        super(paramMediaFormat, paramSurface.getmSurface(), callback);
        mOutputSurface = paramSurface;
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
        }
        mBufferInfo = new MediaCodec.BufferInfo();
        int decoderStatus = mDecoder.dequeueOutputBuffer(mBufferInfo,10000);
        if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            Log.d("mytest", "video no output from decoder available");
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
        } else {
            if(mBufferInfo.size > 0){
                Log.d("mytest","video avalible");
                mDecoder.releaseOutputBuffer(decoderStatus,true);
            }else{
                mDecoder.releaseOutputBuffer(decoderStatus,false);
            }
            mFrameToProcess = decoderStatus;
        }
    }

    @Override
    public void outputFrame() {
        if(mFrameToProcess == -1){
            return;
        }
        if(mEncoder == null){
            return;
        }
        if(mInputSurface == null){
            return;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            mEncoder.signalEndOfInputStream();
            mFrameToProcess = -1;
            stageComplete();
            return;
        }
        mOutputSurface.drawImage();
        mInputSurface.setPresentationTime(mBufferInfo.presentationTimeUs*1000L);
        mInputSurface.swapBuffers();
        mFrameToProcess = -1;
    }
    public void setmEncoder(MediaCodec encoder){
        mEncoder = encoder;
    }
    public void setmInputSurface(InputSurface surface){
        mInputSurface = surface;
    }
    public void setFilter(GPUImageFilter filter){
        if(mOutputSurface != null){
            mOutputSurface.setFilter(filter,true);
        }
    }
}
