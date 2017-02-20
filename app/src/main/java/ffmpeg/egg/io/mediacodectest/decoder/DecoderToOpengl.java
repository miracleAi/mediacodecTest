package ffmpeg.egg.io.mediacodectest.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.extractor.ExtractorToDecoder;
import ffmpeg.egg.io.mediacodectest.openglbase.InputSurface;
import ffmpeg.egg.io.mediacodectest.openglbase.OutputSurface;

/**
 * Created by zhulinping on 17/2/7.
 */

public class DecoderToOpengl {
    private MediaCodec mAudioDecoder;
    private MediaCodec mVideoDecoder;
    private ExtractorToDecoder mExtractor;
    private MediaCodec mAudioEncoder;
    private MediaCodec mVideoEncoder;
    final int TIMEOUT_USEC = 10000;
    private OutputSurface mOutputSurface;
    private InputSurface mInputSurface;
    Surface surface;

    public DecoderToOpengl(ExtractorToDecoder extractor) {
        mExtractor = extractor;
        initDecoder();
    }

    public DecoderToOpengl(Surface surface,ExtractorToDecoder extractor) {
        mExtractor = extractor;
        this.surface = surface;
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
            mOutputSurface = new OutputSurface();
            mVideoDecoder.configure(videoFormat,mOutputSurface.getmSurface(), null, 0);
            //mVideoDecoder.configure(videoFormat,surface, null, 0);
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
            return;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            mAudioDecoder.releaseOutputBuffer(outputIndex, false);
            return;
        }
        int index = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
        if (index == -1) {
            Log.d("AUDIODECODER", "no audio encoder input buffer");
            return;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            mAudioEncoder.signalEndOfInputStream();
        }
        ByteBuffer localByteBuffer = inputBuffers[index];
        int size = mBufferInfo.size;
        if (size >= 0) {
            ByteBuffer buffer = outputBuffers[outputIndex];
            localByteBuffer.position(0);
            localByteBuffer.put(buffer);
            mAudioEncoder.queueInputBuffer(index, 0, size, mBufferInfo.presentationTimeUs, mBufferInfo.flags);
        }
        mAudioDecoder.releaseOutputBuffer(outputIndex, false);
    }

    public void videoDecoder() {
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
                mVideoEncoder.signalEndOfInputStream();
                Log.d("mytest", "video output EOS");
                return;
            }
            Log.d("mytest", "video output info"+mBufferInfo.size+"time"+mBufferInfo.presentationTimeUs);
            mOutputSurface.awaitNewImage();
            mOutputSurface.drawImage();
            mInputSurface.setPresentationTime(mBufferInfo.presentationTimeUs*1000);
            mInputSurface.swapBuffers();
        }
    }

    public void releaseAudio(){
        mAudioDecoder.stop();
        mAudioDecoder.release();
        mExtractor.releaseAudioExtractor();
    }
    public void releaseVideo(){
        mVideoDecoder.stop();
        mVideoDecoder.release();
        mExtractor.releaseVideoExtractor();
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
    public void setInputSurface(InputSurface surface){
        mInputSurface = surface;
    }
}
