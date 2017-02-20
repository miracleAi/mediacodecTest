package ffmpeg.egg.io.mediacodectest.edit.utils;

/**
 * Created by zhulinping on 17/2/6.
 */

public abstract interface DoneCallback {
    public abstract void  audioDone();
    public abstract void videoDone();
}
