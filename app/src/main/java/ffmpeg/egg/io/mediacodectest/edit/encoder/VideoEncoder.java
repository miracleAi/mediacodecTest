package ffmpeg.egg.io.mediacodectest.edit.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.edit.muxer.Muxer;
import ffmpeg.egg.io.mediacodectest.edit.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.edit.utils.EncoderConfiguration;
import ffmpeg.egg.io.mediacodectest.edit.utils.MimeTools;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;


/**
 * Created by zhulinping on 17/2/15.
 */

public class VideoEncoder extends BaseEncoder {
    private InputSurface mInputSurface;

    protected VideoEncoder(Muxer muxer, StageDoneCallback callback) {
        super(muxer, callback);
    }

    public VideoEncoder(Muxer muxer, EncoderConfiguration config, StageDoneCallback callback) {
        this(muxer, callback);
        try {
            // mEncoder = MediaCodec.createEncoderByType(mimeType);
            mEncoder = MediaCodec.createByCodecName(MimeTools.getInstance().selectCodec(config.getMimeType()).getName());
            mEncoder.configure(config.getFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = new InputSurface(mEncoder.createInputSurface());
            mInputSurface.makeCurrent();
            mEncoder.start();
            Log.d("mytest","video encoder start");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int addOrRetrieveMixerTrack(MediaFormat paramMediaFormat) {
        int trackIndex = muxer.addTrack(paramMediaFormat);

        return trackIndex;
    }

    public InputSurface getInputSurface() {
        return mInputSurface;
    }

    public void release() {
        super.release();
        try {
            if (mInputSurface != null) {
                mInputSurface.release();
                mInputSurface = null;
            }
            return;
        } catch (Exception localException) {
        }
    }
}
