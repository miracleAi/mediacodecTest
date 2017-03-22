package ffmpeg.egg.io.mediacodectest.task;


import ffmpeg.egg.io.mediacodectest.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;

/**
 * Created by zhulinping on 17/2/16.
 */

public class RenderTask implements Runnable{
    private Render mRender;
    private InputSurface mSurface;
    private String mPath;
    private TranscodingResources mResources;
    private GPUImageFilter mFilter;
    private boolean mSilence = false;
    public RenderTask(InputSurface surface, String path, TranscodingResources resources){
        mSurface = surface;
        mPath = path;
        mResources = resources;
    }
    @Override
    public void run() {
        mRender = new Render(mSurface,mPath,mResources);
        mRender.setFilter(mFilter);
        mRender.setSilence(mSilence);
        mRender.prepareThread();
        mRender.render();
        mRender.release();
    }
    public void setFilter(GPUImageFilter filter){
        if(mRender != null){
            mRender.setFilter(filter);
        }else{
            mFilter = filter;
        }
    }
    public void setSilence(boolean isSilence){
        if(mRender != null){
            mRender.setSilence(isSilence);
        }else{
            mSilence = isSilence;
        }
    }
    public void tryAbort(){
        if(mRender != null){
            mRender.abort();
        }
    }
}
