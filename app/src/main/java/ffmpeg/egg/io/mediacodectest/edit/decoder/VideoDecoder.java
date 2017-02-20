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
 * Created by zhulinping on 17/2/15.
 */

public class VideoDecoder extends BaseDecoder {
    protected OutputSurface mOutputSurface;
    private MediaCodec mEncoder;
    protected InputSurface mInputSurface;
    MediaCodec.BufferInfo mBufferInfo;

    public VideoDecoder(MediaFormat paramMediaFormat, TranscodingResources resources, StageDoneCallback callback) {
        this(paramMediaFormat, new OutputSurface(resources), callback);
    }

    public VideoDecoder(MediaFormat paramMediaFormat, OutputSurface paramSurface, StageDoneCallback callback) {
        super(paramMediaFormat, paramSurface.getmSurface(), callback);
        mOutputSurface = paramSurface;
    }

    @Override
    public void getFrameFromDecoder() {
        mBufferInfo = new MediaCodec.BufferInfo();
        int decoderStatus = mDecoder.dequeueOutputBuffer(mBufferInfo, 10000L);
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
                Log.d("mytest", "video output true");
                mDecoder.releaseOutputBuffer(decoderStatus, true);
            } else {
                Log.d("mytest", "video output false");
                mDecoder.releaseOutputBuffer(decoderStatus, false);
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
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            mEncoder.signalEndOfInputStream();
            mFrameToProcess = -1;
            stageComplete();
            return;
        }
        if (mInputSurface == null) {
            Log.d(TAG, "Error getting encoder input surface");
            return;
        }
        mOutputSurface.drawImage();
        mInputSurface.setPresentationTime(mBufferInfo.presentationTimeUs * 1000L);
        mInputSurface.swapBuffers();
        mFrameToProcess = -1;
    }

    @Override
    public void processFrame() {
        if (mFrameToProcess == -1) {
            getFrameFromDecoder();
        }
        outputFrame();
    }
    public void setFilter(GPUImageFilter filter){
        mOutputSurface.setFilter(filter,true);
    }
    public void setmEncoder(MediaCodec encoder){
        mEncoder = encoder;
    }
    public void setmInputSurface(InputSurface surface){
        mInputSurface = surface;
    }

}
