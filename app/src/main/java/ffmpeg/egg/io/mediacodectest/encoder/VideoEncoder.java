package ffmpeg.egg.io.mediacodectest.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.muxer.Muxer;
import ffmpeg.egg.io.mediacodectest.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.utils.EncoderConfiguration;
import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/3/22.
 */

public class VideoEncoder extends BaseEncoder {
    private InputSurface mInputSurface;

    protected VideoEncoder(Muxer muxer, StageDoneCallback callback) {
        super(muxer, callback);
    }

    public VideoEncoder(Muxer muxer, EncoderConfiguration config, StageDoneCallback callback) {
        this(muxer, callback);
        try {
            mEncoder = MediaCodec.createEncoderByType(config.getMimeType());
            mEncoder.configure(config.getFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = new InputSurface(mEncoder.createInputSurface());
            mInputSurface.makeCurrent();
            mEncoder.start();
            Log.d("mytest", "video encoder start");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public InputSurface getmInputSurface(){
        return mInputSurface;
    }

    @Override
    public int addOrRetrieveMixerTrack(MediaFormat paramMediaFormat) {
        int trackIndex = muxer.addTrack(paramMediaFormat);
        return trackIndex;
    }

    @Override
    public void release() {
        super.release();
        if(mInputSurface != null){
            mInputSurface.release();
            mInputSurface = null;
        }
    }
}
