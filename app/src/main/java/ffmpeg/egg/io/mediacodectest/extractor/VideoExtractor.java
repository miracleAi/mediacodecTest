package ffmpeg.egg.io.mediacodectest.extractor;

import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;
import ffmpeg.egg.io.mediacodectest.utils.MimeTools;

/**
 * Created by zhulinping on 17/3/21.
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
