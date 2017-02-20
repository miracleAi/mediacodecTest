package ffmpeg.egg.io.mediacodectest.edit.extractor;


import ffmpeg.egg.io.mediacodectest.edit.utils.MimeTools;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/2/15.
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
