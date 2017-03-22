package ffmpeg.egg.io.mediacodectest.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.muxer.Muxer;
import ffmpeg.egg.io.mediacodectest.utils.Stage;
import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/3/22.
 */

public abstract class BaseEncoder extends Stage{
    protected MediaCodec mEncoder;
    protected Muxer muxer;
    protected int mOutputTrack = -1;

    protected BaseEncoder(Muxer muxer,StageDoneCallback callback) {
        super(callback);
        this.muxer = muxer;
    }

    public MediaCodec getCodec() {
        return mEncoder;
    }

    public abstract int addOrRetrieveMixerTrack(MediaFormat paramMediaFormat);


    @Override
    public void processFrame() {
        if(mEncoder == null){
            throw new NullPointerException("encoder null");
        }
        if ((mOutputTrack != -1) && (!muxer.isStarted())) {
            Log.d("ENCODER", "Mixer is not started returning");
            return;
        }
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mEncoder.getOutputBuffers();
        int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, 10000L);
        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            Log.d("mytest", "no output available, spinning to await EOS");
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not expected for an encoder
            outputBuffers = mEncoder.getOutputBuffers();
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // should happen before receiving buffers, and should only happen once
            MediaFormat newFormat = mEncoder.getOutputFormat();
            mOutputTrack = addOrRetrieveMixerTrack(newFormat);
            muxer.startMuxer();
            // now that we have the Magic Goodies, start the muxer
        } else {
            if (!muxer.isStarted()) {
                return;
            }
            ByteBuffer encodedData = outputBuffers[encoderStatus];
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                // The codec config data was pulled out and fed to the muxer when we got
                // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                mBufferInfo.size = 0;
                mEncoder.releaseOutputBuffer(encoderStatus, false);
                return;
            }
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                stageComplete();
                return;
            }
            if (mBufferInfo.size != 0) {
                // adjust the ByteBuffer values to match BufferInfo (not needed?)
                encodedData.position(mBufferInfo.offset);
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                muxer.writeSampleData(mOutputTrack, encodedData, mBufferInfo);
            }
            mEncoder.releaseOutputBuffer(encoderStatus, false);
        }
    }
    public void release(){
        if(mEncoder != null){
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
    }
}
