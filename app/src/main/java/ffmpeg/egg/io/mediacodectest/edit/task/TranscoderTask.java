package ffmpeg.egg.io.mediacodectest.edit.task;


import ffmpeg.egg.io.mediacodectest.edit.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;

/**
 * Created by zhulinping on 17/2/15.
 */

public class TranscoderTask implements Runnable{
    private TranscodingResources mResourdes;
    private String mPath;
    private Transcoder mTransCoder;
    private GPUImageFilter mFilter;
    private boolean mSilence = false;
    private String mOutput;

    public TranscoderTask(TranscodingResources resoueces, String path,String output){
        mResourdes = resoueces;
        mPath = path;
        mOutput = output;
    }
    public void setmFilter(GPUImageFilter filter){
        mFilter = filter;
    }
    public void setSilence(boolean isSilence){
        mSilence = isSilence;
    }
    @Override
    public void run() {
        mTransCoder = new Transcoder(mResourdes,mPath,mOutput);
        mTransCoder.setFilter(mFilter);
        mTransCoder.setSilence(mSilence);
        mTransCoder.transcoder();
    }
}
