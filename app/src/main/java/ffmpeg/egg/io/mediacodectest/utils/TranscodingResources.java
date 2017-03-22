package ffmpeg.egg.io.mediacodectest.utils;

import android.content.Context;

import ffmpeg.egg.io.mediacodectest.filters.IFNomalFilter;


/**
 * Created by zhulinping on 16/11/24.
 */

public class TranscodingResources {
    private Context mContext;
    private IFNomalFilter nomalFilter;
    private int surfaceWidth;
    private int surfaceHeight;
    private int videoWidth;
    private int videoHeight;
    private int videoRotation;
    public TranscodingResources(Context context){
        mContext = context;
    }
    public IFNomalFilter getNomalFilter(){
        return new IFNomalFilter(mContext);
    }
    public void setSurfaceWidth(int width){
        this.surfaceWidth = width;
    }
    public void setSurfaceHeight(int height){
        this.surfaceHeight = height;
    }
    public void setVideoWidth(int width){
        this.videoWidth = width;
    }
    public void setVideoHeight(int height){
        this.videoHeight = height;
    }
    public void setVideoRotation(int rotation){
        this.videoRotation = rotation;
    }
    public int getVideoRotation(){
        return videoRotation;
    }
    public int getSurfaceWidth(){
        return  surfaceWidth;
    }
    public int getSurfaceHeight(){
        return surfaceHeight;
    }
    public int getVideoWidth(){
        return  videoWidth;
    }
    public int getVideoHeight(){
        return videoHeight;
    }
    public Context getmContext(){
        return mContext;
    }
}
