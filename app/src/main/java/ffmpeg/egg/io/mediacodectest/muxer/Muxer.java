package ffmpeg.egg.io.mediacodectest.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.utils.MimeTools;

/**
 * Created by zhulinping on 17/2/13.
 */

public class Muxer {
    private MediaMuxer mMuxer;
    private int mAudioTrack = -1;
    private int mVideoTrack = -1;
    private int mTrackIndex = -1;
    private boolean mIsSilence = false;
    private boolean mIsMuxerStart = false;

    public Muxer(String path) {
        try {
            mMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int addTrack(MediaFormat format) {
        if (MimeTools.getInstance().isAudioFormat(format)) {
            if (mAudioTrack == -1) {
                mAudioTrack = mMuxer.addTrack(format);
            }
            mTrackIndex = mAudioTrack;
        } else if (MimeTools.getInstance().isVideoFormat(format)) {
            if (mVideoTrack == -1) {
                mVideoTrack = mMuxer.addTrack(format);
            }
            mTrackIndex = mVideoTrack;
        }
        return mTrackIndex;
    }

    public void setmIsSilence(boolean isSilence) {
        mIsSilence = isSilence;
    }

    public boolean isStarted() {
        return mIsMuxerStart;
    }

    public void startMuxer() {
        if (mIsSilence) {
            if (mVideoTrack != -1 && !isStarted()) {
                mMuxer.start();
                Log.d("mytest","muxer started");
                mIsMuxerStart = true;
            }
        } else {
            if (mAudioTrack != -1 && mVideoTrack != -1 && !isStarted()) {
                mMuxer.start();
                Log.d("mytest","muxer started");
                mIsMuxerStart = true;
            }
        }
    }

    public void stopMuxer() {
        if (isStarted()) {
            mMuxer.stop();
            mMuxer.release();
        }
    }

    public void writeSampleData(int trackIndex, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        mMuxer.writeSampleData(trackIndex, buffer, bufferInfo);
    }
}
