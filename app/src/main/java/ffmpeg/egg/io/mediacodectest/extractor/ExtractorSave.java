package ffmpeg.egg.io.mediacodectest.extractor;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.activity.ExtractorActivity;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

/**
 * Created by zhulinping on 17/3/20.
 */

public class ExtractorSave {
    String mOutputVideoPath = "";
    String mPath = "";
    MediaMuxer mMediaMuxer = null;
    MediaExtractor mAudioExtractor = new MediaExtractor();
    MediaExtractor mVideoExtractor = new MediaExtractor();
    int audioTrackIndex = -1;
    int videoTrackIndex = -1;
    boolean isAudioExtractorDone = false;
    boolean isVideoExtractorDone = false;
    DoneCallback mCallback;
    private int mType = -1;

    public ExtractorSave(String filePath, int type, DoneCallback callback) {
        mCallback = callback;
        mPath = filePath;
        mType = type;
        mOutputVideoPath = Environment.getExternalStorageDirectory().toString()
                + "/dcim/camera/" + System.currentTimeMillis() + ".mp4";
        try {
            mMediaMuxer = new MediaMuxer(mOutputVideoPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAllTrack() {
        try {
            mMediaMuxer = new MediaMuxer(mOutputVideoPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            mAudioExtractor.setDataSource(mPath);
            for (int i = 0; i < mAudioExtractor.getTrackCount(); i++) {
                MediaFormat format = mAudioExtractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    mAudioExtractor.selectTrack(i);
                    audioTrackIndex = mMediaMuxer.addTrack(format);
                    break;
                }
            }
            mVideoExtractor.setDataSource(mPath);
            for (int i = 0; i < mVideoExtractor.getTrackCount(); i++) {
                MediaFormat format = mVideoExtractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                    mVideoExtractor.selectTrack(i);
                    videoTrackIndex = mMediaMuxer.addTrack(format);
                    break;
                }
            }
            Log.d("mytest", "audio" + audioTrackIndex + "video" + videoTrackIndex);
            // 添加完所有轨道后start
            mMediaMuxer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addVideoTrack() {
        try {
            mVideoExtractor.setDataSource(mPath);
            for (int i = 0; i < mVideoExtractor.getTrackCount(); i++) {
                MediaFormat format = mVideoExtractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                    mVideoExtractor.selectTrack(i);
                    videoTrackIndex = mMediaMuxer.addTrack(format);
                    break;
                }
            }
            mMediaMuxer.start();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void addAudioTrack() {
        try {
            mAudioExtractor.setDataSource(mPath);
            for (int i = 0; i < mAudioExtractor.getTrackCount(); i++) {
                MediaFormat format = mAudioExtractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    mAudioExtractor.selectTrack(i);
                    audioTrackIndex = mMediaMuxer.addTrack(format);
                    break;
                }
            }
            mMediaMuxer.start();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void extractorAudio() {
        // 封装音频track
        if (-1 != audioTrackIndex) {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
            while (true) {
                int sampleSize = mAudioExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }

                info.offset = 0;
                info.size = sampleSize;
                info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                info.presentationTimeUs = mAudioExtractor.getSampleTime();
                mMediaMuxer.writeSampleData(audioTrackIndex, buffer, info);

                mAudioExtractor.advance();
            }
            // 释放MediaExtractor
            mAudioExtractor.release();
            isAudioExtractorDone = true;
            mCallback.audioDone();
            release();
        }
    }

    public void extractorVideo() {
        // 封装视频track
        if (-1 != videoTrackIndex) {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(1000 * 1024);
            while (true) {
                int sampleSize = mVideoExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }

                info.offset = 0;
                info.size = sampleSize;
                info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                info.presentationTimeUs = mVideoExtractor.getSampleTime();
                mMediaMuxer.writeSampleData(videoTrackIndex, buffer, info);

                mVideoExtractor.advance();
            }
            mVideoExtractor.release();
            isVideoExtractorDone = true;
            mCallback.videoDone();
            release();
        }
    }


    public void release() {
        // 释放MediaMuxer
        switch (mType) {
            case ExtractorActivity.ALL_TYPE:
                if (isAudioExtractorDone && isVideoExtractorDone) {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                }
                break;
            case ExtractorActivity.AUDIO_TYPE:
                if (isAudioExtractorDone) {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                }
                break;
            case ExtractorActivity.VIDEO_TYPE:
                if (isVideoExtractorDone) {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                }
                break;
        }

    }
}
