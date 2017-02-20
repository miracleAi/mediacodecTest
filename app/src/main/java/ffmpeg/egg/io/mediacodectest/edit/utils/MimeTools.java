package ffmpeg.egg.io.mediacodectest.edit.utils;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by zhulinping on 16/11/22.
 */

public class MimeTools {
    private static final String TAG = "MimeTools";
    private static final MimeTools sInstance = new MimeTools();

    public static MimeTools getInstance() {
        return sInstance;
    }

    public MediaExtractor createExtractor(String paramString) {
        MediaExtractor localMediaExtractor = new MediaExtractor();
        Log.d("MimeTools", "Setting video to be " + paramString);
        try {
            localMediaExtractor.setDataSource(paramString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return localMediaExtractor;
    }

    public int getAudioChannelCount(MediaFormat paramMediaFormat) {
        return paramMediaFormat.getInteger("channel-count");
    }

    public int getAudioSampleRate(MediaFormat paramMediaFormat) {
        return paramMediaFormat.getInteger("sample-rate");
    }

    public boolean isAudioFormat(MediaFormat paramMediaFormat) {
        return getMimeTypeFor(paramMediaFormat).startsWith("audio/");
    }

    public boolean isVideoFormat(MediaFormat paramMediaFormat) {
        return getMimeTypeFor(paramMediaFormat).startsWith("video/");
    }

    public String getMimeTypeFor(MediaFormat paramMediaFormat) {
        String mediaFormatStr = "";
        mediaFormatStr = paramMediaFormat.getString(MediaFormat.KEY_MIME);
        return mediaFormatStr;
    }

    public int getVideoTrack(MediaExtractor paramMediaExtractor) {
        int i = 0;
        int count = paramMediaExtractor.getTrackCount();
        while (i < count) {
            Log.d("MimeTools", "format for track " + i + " is " + getMimeTypeFor(paramMediaExtractor.getTrackFormat(i)));
            if (isVideoFormat(paramMediaExtractor.getTrackFormat(i))) {
                Log.d("MimeTools", "Selecting track:" + i);
                return i;
            }
            i += 1;
        }
        return -1;
    }

    public int getAudioTrack(MediaExtractor paramMediaExtractor) {
        int i = 0;
        while (i < paramMediaExtractor.getTrackCount()) {
            Log.d("MimeTools", "format for track:" + i + " is " + getMimeTypeFor(paramMediaExtractor.getTrackFormat(i)));
            if (isAudioFormat(paramMediaExtractor.getTrackFormat(i))) {
                Log.d("MimeTools", "Selecting track:" + i);
                return i;
            }
            i += 1;
        }
        return -1;
    }

    public int getAndSelectVideoTrackIndex(MediaExtractor paramMediaExtractor) {
        int i = getVideoTrack(paramMediaExtractor);
        if (i != -1) {
            paramMediaExtractor.selectTrack(i);
        }
        return i;
    }

    public int getAndSelectAudioTrackIndex(MediaExtractor paramMediaExtractor) {
        int i = getAudioTrack(paramMediaExtractor);
        if (i != -1) {
            paramMediaExtractor.selectTrack(i);
        }
        return i;
    }

    public MediaCodecInfo selectCodec(String paramString) {
        Log.d("MimeTools", "Finding codec for mimeType: " + paramString);
        int k = MediaCodecList.getCodecCount();
        int i = 0;
        while (i < k) {
            MediaCodecInfo localMediaCodecInfo = MediaCodecList.getCodecInfoAt(i);
            if (localMediaCodecInfo.isEncoder()) {
                String[] arrayOfString = localMediaCodecInfo.getSupportedTypes();
                int j = 0;
                while (j < arrayOfString.length) {
                    if (arrayOfString[j].equalsIgnoreCase(paramString)) {
                        Log.d("MimeTools", "Using codec : " + localMediaCodecInfo.getName());
                        return localMediaCodecInfo;
                    }
                    j += 1;
                }
            }
            i += 1;
        }
        return null;
    }
}
