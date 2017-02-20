package ffmpeg.egg.io.mediacodectest.edit.extractor;


import ffmpeg.egg.io.mediacodectest.edit.utils.MimeTools;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/2/15.
 */

public class VideoExtractor extends BaseExtractor{
    private MimeTools mMimeTools;

    public VideoExtractor(String videoPath, StageDoneCallback callback){
        this(videoPath,MimeTools.getInstance(),callback);
    }
    protected VideoExtractor(String videoPath, MimeTools tools,StageDoneCallback callback) {
        super(videoPath,callback);
        mMimeTools = tools;
        setUpTrack();
    }

    private void setUpTrack() {
        mInputTrack = mMimeTools.getAndSelectVideoTrackIndex(mExtractor);
    }

}

