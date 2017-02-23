package ffmpeg.egg.io.mediacodectest.screencast;

import android.media.projection.MediaProjection;

import java.util.concurrent.atomic.AtomicBoolean;

import ffmpeg.egg.io.mediacodectest.edit.muxer.Muxer;

/**
 * Created by zhulinping on 17/2/23.
 */

public class ScreenRecorderTask implements Runnable {
    private static final String TAG = "ScreenRecorder";
    private int mWidth;
    private int mHeight;
    private int mBitRate;
    private int mDpi;
    private String mDstPath;
    private MediaProjection mMediaProjection;
    private Muxer mMuxer;
    private VideoRecorder mVideoRecoder;
    private AtomicBoolean mQuit = new AtomicBoolean(false);


    public ScreenRecorderTask(int width, int height, int bitrate, int dpi, MediaProjection mp, String dstPath) {
        mWidth = width;
        mHeight = height;
        mBitRate = bitrate;
        mDpi = dpi;
        mMediaProjection = mp;
        mDstPath = dstPath;
    }

    /**
     * stop task
     */
    public final void quit() {
        mQuit.set(true);
    }

    public void release() {
        if (mVideoRecoder != null) {
            mVideoRecoder.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        if (mMuxer != null) {
            mMuxer.stopMuxer();
            mMuxer = null;
        }
    }

    @Override
    public void run() {
        try {
            mMuxer = new Muxer(mDstPath);
            mVideoRecoder = new VideoRecorder(mWidth, mHeight, mBitRate, mDpi, mMediaProjection,mMuxer);
            while(!mQuit.get()){
                mVideoRecoder.videoEncoder();
            }
        } finally {
            release();
        }
    }
}
