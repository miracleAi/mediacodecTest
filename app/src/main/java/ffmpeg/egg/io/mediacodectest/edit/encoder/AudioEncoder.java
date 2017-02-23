package ffmpeg.egg.io.mediacodectest.edit.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.edit.muxer.Muxer;
import ffmpeg.egg.io.mediacodectest.edit.utils.EncoderConfiguration;
import ffmpeg.egg.io.mediacodectest.edit.utils.MimeTools;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;


/**
 * Created by zhulinping on 17/2/15.
 */

public class AudioEncoder extends BaseEncoder {
    protected AudioEncoder(Muxer muxer, StageDoneCallback callback) {
        super(muxer, callback);
    }

    public AudioEncoder(Muxer muxer, EncoderConfiguration config, StageDoneCallback callback) {
        this(muxer, callback);
        try {
            mEncoder = MediaCodec.createByCodecName(MimeTools.getInstance()
                    .selectCodec(config.getMimeType()).getName());
            //mEncoder = MediaCodec.createEncoderByType(MimeTools.getInstance().getMimeTypeFor(format));
            mEncoder.configure(config.getFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
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
}
