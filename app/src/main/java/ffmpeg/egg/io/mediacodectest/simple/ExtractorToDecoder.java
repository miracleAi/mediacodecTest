package ffmpeg.egg.io.mediacodectest.simple;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zhulinping on 17/2/7.
 */

public class ExtractorToDecoder {
    private MediaExtractor mVideoExtractor = new MediaExtractor();
    private MediaExtractor mAudioExtractor = new MediaExtractor();
    private MediaCodec mVideoDecoder;
    private MediaCodec mAudioDecoder;
    private String mPath;
    private MediaFormat mAudioFormat;
    private MediaFormat mVideoFormat;
    final int TIMEOUT_USEC = 10000;


    public ExtractorToDecoder(String path) {
        mPath = path;
        initExtractor();
    }

    public void setmVideoDecoder(MediaCodec videoDecoder) {
        mVideoDecoder = videoDecoder;
    }

    public void setmAudioDecoder(MediaCodec audioDecoder) {
        mAudioDecoder = audioDecoder;
    }

    public MediaFormat getmAudioFormat(){
        return mAudioFormat;
    }
    public MediaFormat getmVideoFormat(){
        return mVideoFormat;
    }


    public void audioExtractor() {
        int inputBufIndex = mAudioDecoder.dequeueInputBuffer(TIMEOUT_USEC);
        ByteBuffer[] decoderInputBuffers = mAudioDecoder.getInputBuffers();
        if (inputBufIndex >= 0) {
            ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
            int bufSize = mAudioExtractor.readSampleData(inputBuf, 0);
            if (bufSize < 0) {
                // End of stream -- send empty frame with EOS flag set.
                mAudioDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                long presentationTimeUs = mAudioExtractor.getSampleTime();
                mAudioDecoder.queueInputBuffer(inputBufIndex, 0, bufSize,
                        presentationTimeUs,mAudioExtractor.getSampleFlags());
                mAudioExtractor.advance();
            }
        }else{
            Log.d("mytest","audio input -1");

        }
    }

    public void videoExtractor() {
        int inputBufIndex = mVideoDecoder.dequeueInputBuffer(TIMEOUT_USEC);
        ByteBuffer[] decoderInputBuffers = mVideoDecoder.getInputBuffers();

        if (inputBufIndex >= 0) {
            ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
            int bufSize = mVideoExtractor.readSampleData(inputBuf, 0);
            if (bufSize < 0) {
                // End of stream -- send empty frame with EOS flag set.
                mVideoDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                long presentationTimeUs = mVideoExtractor.getSampleTime();
                mVideoDecoder.queueInputBuffer(inputBufIndex, 0, bufSize,
                        presentationTimeUs,mVideoExtractor.getSampleFlags());
                mVideoExtractor.advance();
            }
        }else{
            Log.d("mytest","video input -1");
        }
    }

    private void initExtractor() {
        try {
            mAudioExtractor.setDataSource(mPath);
            for (int i = 0; i < mAudioExtractor.getTrackCount(); i++) {
                MediaFormat format = mAudioExtractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    mAudioExtractor.selectTrack(i);
                    mAudioFormat = format;
                    break;
                }
            }
            mVideoExtractor.setDataSource(mPath);
            for (int i = 0; i < mVideoExtractor.getTrackCount(); i++) {
                MediaFormat format = mVideoExtractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                    mVideoExtractor.selectTrack(i);
                    mVideoFormat = format;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void releaseAudioExtractor(){
        mAudioExtractor.release();
    }
    public void releaseVideoExtractor(){
        mVideoExtractor.release();
    }
}
