package ffmpeg.egg.io.mediacodectest.filterrecord.filters;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;
import ffmpeg.egg.io.mediacodectest.openglutils.OpenGlUtils;
import ffmpeg.egg.io.mediacodectest.openglutils.Rotation;
import ffmpeg.egg.io.mediacodectest.openglutils.TextureRotationUtil;

/**
 * Created by zhulinping on 17/2/24.
 */

public class BeautyRender {
    private GPUImageFilter mFilter;
    private BeautyFilter mBeautyFilter;
    protected int textureId = OpenGlUtils.NO_TEXTURE;
    /**
     * 顶点坐标
     */
    protected final FloatBuffer gLCubeBuffer;

    /**
     * 纹理坐标
     */
    protected final FloatBuffer gLTextureBuffer;


    /**
     * GLSurfaceView的宽高
     */
    protected int surfaceWidth, surfaceHeight;
    /**
     * 图像宽高
     */
    protected int imageWidth, imageHeight;
    boolean mIsBeauty = false;


    public BeautyRender() {
        gLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);

        gLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)).position(0);
    }

    public void setVideoSize(int width,int height){
        imageWidth = width;
        imageHeight = height;
    }
    public void setSurfaceSize(int width,int height){
        surfaceWidth = width;
        surfaceHeight = height;
    }
    public void init(BeautyFilter filter) {
        if (mBeautyFilter == null)
            mBeautyFilter = filter;
        mBeautyFilter.init();
        textureId = OpenGlUtils.getExternalOESTextureID();
        mBeautyFilter.onInputSizeChanged(imageWidth, imageHeight);
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
    }

    public int getmGLTextureId() {
        return textureId;
    }

    public void setFilter(final GPUImageFilter filter, boolean isTransCode) {
        if (mFilter != null) {
            mFilter.destroy();
            mFilter = null;
        }
        mFilter = filter;
        if(mFilter != null ){
            Log.d("mytest","filter is not null !!!!!!!");
            mFilter.init();
        }
        onFilterChanged(isTransCode);
    }
    private void onFilterChanged(boolean isTransCode) {
        if (mFilter != null) {
            if(isTransCode){
                mFilter.onDisplaySizeChanged(imageWidth, imageHeight);
            }else{
                mFilter.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
            }
            mFilter.onInputSizeChanged(imageWidth, imageHeight);
        }
        if(isTransCode){
            mBeautyFilter.onDisplaySizeChanged(imageWidth, imageHeight);
        }else{
            mBeautyFilter.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
        }
        if (mFilter != null)
            mBeautyFilter.initCameraFrameBuffer(imageWidth, imageHeight);
        else
            mBeautyFilter.destroyFramebuffers();
    }

    public void onDrawFrame(float[] mtx) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mBeautyFilter.setTextureTransformMatrix(mtx);
        int id = textureId;
        if (mFilter == null) {
            mBeautyFilter.onDrawFrame(textureId, gLCubeBuffer, gLTextureBuffer);
        } else {
            float level = 0;
            if(mIsBeauty){
                level = 0.33f;
            }else{
                level = 0.0f;
            }
            id = mBeautyFilter.onDrawToTexture(textureId,level);
            mFilter.onDrawFrame(id, gLCubeBuffer, gLTextureBuffer);
        }
    }
    public void onDrawFrame(float[] mtx,int texId) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mBeautyFilter.setTextureTransformMatrix(mtx);
        int id = texId;
        if (mFilter == null) {
            mBeautyFilter.onDrawFrame(texId, gLCubeBuffer, gLTextureBuffer);
        } else {
            float level = 0;
            if(mIsBeauty){
                level = 0.33f;
            }else{
                level = 0.0f;
            }
            id = mBeautyFilter.onDrawToTexture(texId,level);
            mFilter.onDrawFrame(id, gLCubeBuffer, gLTextureBuffer);
        }
    }
    public void onBeautyChange(boolean isBeauty){
        mIsBeauty = isBeauty;
        if(mBeautyFilter != null){
            if(isBeauty){
            mBeautyFilter.setBeautyLevel(5);
            }else{
                mBeautyFilter.setBeautyLevel(0);
            }
        }
    }
}

