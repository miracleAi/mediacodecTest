package ffmpeg.egg.io.mediacodectest.extractor;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

/**
 * Created by zhulinping on 17/2/6.
 */

public class Extractor {
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

    public Extractor(String filePath, DoneCallback callback) {
        mCallback = callback;
        mPath = filePath;
        mOutputVideoPath = Environment.getExternalStorageDirectory().toString()
                + "/dcim/camera/" + System.currentTimeMillis() + ".mp4";
    }

    public void addTrack() {
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
            Log.d("mytest","audio"+audioTrackIndex+"video"+videoTrackIndex);
            // 添加完所有轨道后start
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
            if (isVideoExtractorDone) {
                release();
            }
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
            if (isAudioExtractorDone) {
                release();
            }
        }
    }

    public void release() {
        // 释放MediaMuxer
        mMediaMuxer.stop();
        mMediaMuxer.release();
    }
}
