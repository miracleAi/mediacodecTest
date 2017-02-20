package ffmpeg.egg.io.mediacodectest.edit.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.edit.muxer.Muxer;
import ffmpeg.egg.io.mediacodectest.edit.surface.InputSurface;
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

    public VideoEncoder(Muxer muxer, MediaFormat format, StageDoneCallback callback) {
        this(muxer, callback);
        try {
            mEncoder = MediaCodec.createEncoderByType(MimeTools.getInstance().getMimeTypeFor(format));
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = new InputSurface(mEncoder.createInputSurface());
            mInputSurface.makeCurrent();
            mEncoder.start();
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
