package ffmpeg.egg.io.mediacodectest.extractor;

import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;
import ffmpeg.egg.io.mediacodectest.utils.MimeTools;

/**
 * Created by zhulinping on 17/3/21.
 */

public class AudioExtractor extends BaseExtractor{
    MimeTools mMimeTools;
    public AudioExtractor(String videoPath,StageDoneCallback callback){
        this(videoPath,MimeTools.getInstance(),callback);

    }
    protected AudioExtractor(String videoPath, MimeTools tools, StageDoneCallback callback) {
        super(videoPath,callback);
        this.mMimeTools = tools;
        setupInputTrack();
    }

    private void setupInputTrack() {
        mInputTrack = mMimeTools.getAndSelectAudioTrackIndex(mExtractor);
    }
}
