package ffmpeg.egg.io.mediacodectest.edit.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.edit.utils.MimeTools;
import ffmpeg.egg.io.mediacodectest.edit.utils.Stage;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;


/**
 * Created by zhulinping on 17/2/15.
 */

public abstract class BaseDecoder extends Stage {
    protected static final String TAG = "DECODER";
    protected MediaCodec mDecoder;
    public int mFrameToProcess = -1;
    protected BaseDecoder(MediaFormat paramMediaFormat, Surface paramSurface, StageDoneCallback callback)
    {
        super(callback);
        try {
            mDecoder = MediaCodec.createDecoderByType(MimeTools.getInstance().getMimeTypeFor(paramMediaFormat));
            mDecoder.configure(paramMediaFormat,paramSurface,null,0);
            mDecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public MediaCodec getmDecoder(){
        return  mDecoder;
    }
    public abstract void getFrameFromDecoder();

    public abstract void outputFrame();

    public void release()
    {
        if (mDecoder != null) {
            mDecoder.stop();
            mDecoder.release();
            mDecoder = null;
        }
    }
}