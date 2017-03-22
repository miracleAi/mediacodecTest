package ffmpeg.egg.io.mediacodectest.decoderplay;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/3/21.
 */

public class AudioTrackDecoder extends BaseDecoder {
    private AudioTrack mAudioTrack;
    MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    public AudioTrackDecoder(MediaFormat paramMediaFormat, StageDoneCallback callback) {
        super(paramMediaFormat, null, callback);
        int j = paramMediaFormat.getInteger("sample-rate");
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, j, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, j, AudioTrack.MODE_STREAM);
        mAudioTrack.setPlaybackRate(j);
        mAudioTrack.play();
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
        if (mDecoder == null) {
            return;
        }
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
        if (mFrameToProcess == -1) {
            return;
        }
        if (mDecoder == null) {
            stageComplete();
            return;
        }
        ByteBuffer[] decoderoutputBuffers = mDecoder.getOutputBuffers();
        int size = mBufferInfo.size;
        if (size >= 0) {
            ByteBuffer localByteBuffer = decoderoutputBuffers[mFrameToProcess];
            byte[] arrayOfByte = new byte[size];
            localByteBuffer.get(arrayOfByte);
            localByteBuffer.clear();
            mAudioTrack.write(arrayOfByte, 0, size);
        }
        mDecoder.releaseOutputBuffer(mFrameToProcess, false);
        mFrameToProcess = -1;
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            stageComplete();
        }
    }

    public void release() {
        super.release();
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public void setVolume(boolean isSilence) {
        if (isSilence) {
            if (this.mAudioTrack != null) {
                this.mAudioTrack.setStereoVolume(0.0f, 0.0f);
            }
        } else {
            if (this.mAudioTrack != null) {
                this.mAudioTrack.setStereoVolume(1.0f, 1.0f);
            }
        }
    }
    public void restart(){
        if(mDecoder != null){
            mDecoder.flush();
        }
    }
}