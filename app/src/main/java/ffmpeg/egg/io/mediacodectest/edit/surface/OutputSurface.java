package ffmpeg.egg.io.mediacodectest.edit.surface;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import ffmpeg.egg.io.mediacodectest.edit.render.FilterRender;
import ffmpeg.egg.io.mediacodectest.edit.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;
import ffmpeg.egg.io.mediacodectest.openglutils.ImageTransformationMatrix;

/**
 * Created by zhulinping on 16/11/21.
 */

public class OutputSurface implements SurfaceTexture.OnFrameAvailableListener {
    private Surface mSurface;
    private TranscodingResources mResources;
    protected SurfaceTexture mSurfaceTexture;
    protected FilterRender mRender;
    private int textureId;
    public OutputSurface(TranscodingResources resources) {
        mResources = resources;
        setup();
    }

    private void setup() {
        mRender = new FilterRender();
        mRender.setVideoSize(mResources.getVideoWidth(),mResources.getVideoHeight());
        mRender.setSurfaceSize(mResources.getSurfaceWidth(),mResources.getSurfaceHeight());
        mRender.init(mResources.getNomalFilter());
        textureId = mRender.getmGLTextureId();
        mSurfaceTexture = new SurfaceTexture(textureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);
    }

    public Surface getmSurface() {
        return mSurface;
    }

    public void setFilter(GPUImageFilter filter, boolean isTransCode) {
        mRender.setFilter(filter,isTransCode);
    }
    private float[] mSTMatrix = new float[16];

    public void drawImage() {
        mSurfaceTexture.updateTexImage();
        if(mResources.getVideoRotation() == 0){
            mSTMatrix = new ImageTransformationMatrix().adjustForTranscoderTransformation();
            //mSurfaceTexture.getTransformMatrix(mSTMatrix);
        }else if(mResources.getVideoRotation() == 90){
             mSTMatrix = new ImageTransformationMatrix().rotateRight();
        }else if(mResources.getVideoRotation() == 180){
            mSTMatrix = new ImageTransformationMatrix().rotate180Degrees();
        }else{
            mSTMatrix = new ImageTransformationMatrix().rotateLeft();
        }
        mRender.onDrawFrame(mSTMatrix);
    }

    public void release() {
        mSurface.release();
        mSurface = null;
        mSurfaceTexture = null;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }
}
