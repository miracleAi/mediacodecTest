package ffmpeg.egg.io.mediacodectest.edit.utils;

import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.IOException;

public class VideoMetadataReader {
    private static final String TAG = "VideoMetadataReader";
    private MediaMetadataRetriever mMediaMetadataRetriever;
    private final File mVideoFile;

    public VideoMetadataReader(File file) {
        this(file, new MediaMetadataRetriever());
    }

    protected VideoMetadataReader(File file, MediaMetadataRetriever mediaMetadataRetriever) {
        if (file == null) {
            throw new NullPointerException("videoFile is null");
        }
        this.mVideoFile = file;
        this.mMediaMetadataRetriever = mediaMetadataRetriever;
        this.mMediaMetadataRetriever.setDataSource(file.toString());
    }

    public void release() {
        if (!released()) {
            this.mMediaMetadataRetriever.release();
            this.mMediaMetadataRetriever = null;
        }
    }

    public int getWidth() {
        checkPreconditions();
        String metadata = getMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        return Integer.parseInt(metadata);
    }

    public int getHeight() {
        checkPreconditions();
        String metadata = getMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        return Integer.parseInt(metadata);
    }

    public long getDurationMs() {
        checkPreconditions();
        String metadata = getMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(metadata);
    }

    public String getMimeType() {
        checkPreconditions();
        return getMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
    }

    public void finalize() {
        if (!released()) {
            release();
        }
    }

    private void checkExists() {
        if (!this.mVideoFile.exists()) {
            try {
                throw new IOException("File not found: " + this.mVideoFile.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkNotReleased() {
        if (released()) {
            throw new IllegalStateException("VideoMetadataReader already released!");
        }
    }

    private void checkPreconditions() {
        checkNotReleased();
        checkExists();
    }

    public int getRotation(){
        checkPreconditions();
        String rotation = getMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        return Integer.parseInt(rotation);
    }

    private boolean released() {
        return this.mMediaMetadataRetriever == null;
    }

    private String getMetadata(int i) {
        String extractMetadata = this.mMediaMetadataRetriever.extractMetadata(i);
            return extractMetadata;
    }
}
