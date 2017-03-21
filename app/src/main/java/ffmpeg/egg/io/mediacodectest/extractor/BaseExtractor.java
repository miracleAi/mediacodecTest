package ffmpeg.egg.io.mediacodectest.extractor;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;
import ffmpeg.egg.io.mediacodectest.utils.MimeTools;
import ffmpeg.egg.io.mediacodectest.utils.Stage;

/**
 * Created by zhulinping on 17/3/21.
 */

public class BaseExtractor extends Stage {
    protected static final String TAG = "EXTRACTOR";

    protected MediaExtractor mExtractor;
    protected MediaCodec mDecoder;
    public int mInputTrack = -1;
    private boolean mShouldAbort = false;

    protected BaseExtractor(String videoPath, StageDoneCallback stageDoneCallback) {
        super(stageDoneCallback);
        mExtractor = MimeTools.getInstance().createExtractor(videoPath);
    }

    public MediaFormat getFormat() {
        if (mInputTrack == -1) {
            return null;
        }
        return mExtractor.getTrackFormat(mInputTrack);
    }

    public void processFrame() {
        if (mDecoder == null) {
            return;
        }
        int dequeueInputBuffer = mDecoder.dequeueInputBuffer(10000L);
        ByteBuffer[] buffers = mDecoder.getInputBuffers();
        if (dequeueInputBuffer != -1) {
            int readSampleData = this.mExtractor.readSampleData(buffers[dequeueInputBuffer], 0);
            if (readSampleData < 0 || mShouldAbort) {
                this.mDecoder.queueInputBuffer(dequeueInputBuffer, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                stageComplete();
                return;
            }
            long sampleTime = this.mExtractor.getSampleTime();
            mDecoder.queueInputBuffer(dequeueInputBuffer, 0, readSampleData, sampleTime, this.mExtractor.getSampleFlags());
            mExtractor.advance();
        }
    }

    public void setDecoder(MediaCodec decoder) {
        mDecoder = decoder;
    }

    public void restart() {
        if (mExtractor != null) {
            mExtractor.seekTo(0L, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
    }

    public void release() {
        try {
            if (mExtractor != null) {
                mExtractor.release();
                mExtractor = null;
            }
            return;
        } catch (Exception localException) {
        }
    }
    public void abort() {
        this.mShouldAbort = true;
    }

}


