package ffmpeg.egg.io.mediacodectest.filterrecord.encoder;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: RenderHandler.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import ffmpeg.egg.io.mediacodectest.filterrecord.filters.BeautyFilter;
import ffmpeg.egg.io.mediacodectest.filterrecord.filters.BeautyRender;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;
import ffmpeg.egg.io.mediacodectest.recordold.CameraInputFilter;


/**
 * Helper class to draw texture to whole view on private thread
 */
public final class FilterRenderHandler implements Runnable {
    private static final boolean DEBUG = false;	// TODO set false on release
    private static final String TAG = "RenderHandler";

    private final Object mSync = new Object();
    private EGLContext mShard_context;
    private boolean mIsRecordable;
    private Object mSurface;
    private int mTexId = -1;
    private float[] mMatrix = new float[16];

    private boolean mRequestSetEglContext;
    private boolean mRequestRelease;
    private int mRequestDraw;
    private static Context mContext;
    private static int mWidth;
    private static int mHeight;
    private static GPUImageFilter mFilter;

    public static final FilterRenderHandler createHandler(GPUImageFilter filter,int width, int height, Context context, final String name) {
        mContext = context;
        mFilter = filter;
        mWidth = width;
        mHeight = height;
        if (DEBUG) Log.v(TAG, "createHandler:");
        final FilterRenderHandler handler = new FilterRenderHandler();
        synchronized (handler.mSync) {
            new Thread(handler, !TextUtils.isEmpty(name) ? name : TAG).start();
            try {
                handler.mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
        return handler;
    }

    public final void setEglContext(final EGLContext shared_context, final int tex_id, final Object surface, final boolean isRecordable) {
        if (DEBUG) Log.i(TAG, "setEglContext:");
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceHolder))
            throw new RuntimeException("unsupported window type:" + surface);
        synchronized (mSync) {
            if (mRequestRelease) return;
            mShard_context = shared_context;
            mTexId = tex_id;
            mSurface = surface;
            mIsRecordable = isRecordable;
            mRequestSetEglContext = true;
            Matrix.setIdentityM(mMatrix, 0);
            //Matrix.setIdentityM(mMatrix, 16);
            mSync.notifyAll();
            try {
                mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    public final void draw() {
        draw(mTexId, mMatrix, null);
    }

    public final void draw(final int tex_id) {
        draw(tex_id, mMatrix, null);
    }

    public final void draw(final float[] tex_matrix) {
        draw(mTexId, tex_matrix, null);
    }

    public final void draw(final float[] tex_matrix, final float[] mvp_matrix) {
        draw(mTexId, tex_matrix, mvp_matrix);
    }

    public final void draw(final int tex_id, final float[] tex_matrix) {
        draw(tex_id, tex_matrix, null);
    }

    public final void draw(final int tex_id, final float[] tex_matrix, final float[] mvp_matrix) {
        synchronized (mSync) {
            if (mRequestRelease) return;
            mTexId = tex_id;
			/*if ((tex_matrix != null) && (tex_matrix.length >= 16)) {
				System.arraycopy(tex_matrix, 0, mMatrix, 0, 16);
			} else {
				Matrix.setIdentityM(mMatrix, 0);
			}*/
            if ((mvp_matrix != null) && (mvp_matrix.length >= 16)) {
                System.arraycopy(mvp_matrix, 0, mMatrix, 0, 16);
            } else {
                Matrix.setIdentityM(mMatrix, 0);
            }
            mRequestDraw++;
            mSync.notifyAll();
        }
    }

    public boolean isValid() {
        synchronized (mSync) {
            return !(mSurface instanceof Surface) || ((Surface)mSurface).isValid();
        }
    }

    public final void release() {
        if (DEBUG) Log.i(TAG, "release:");
        synchronized (mSync) {
            if (mRequestRelease) return;
            mRequestRelease = true;
            mSync.notifyAll();
            try {
                mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    //********************************************************************************
//********************************************************************************
    private FilterEGLBase mEgl;
    private FilterEGLBase.EglSurface mInputSurface;
    //private CameraInputFilter mFilter;
    private BeautyRender mRender;

    @Override
    public final void run() {
        if (DEBUG) Log.i(TAG, "RenderHandler thread started:");
        synchronized (mSync) {
            mRequestSetEglContext = mRequestRelease = false;
            mRequestDraw = 0;
            mSync.notifyAll();
        }
        boolean localRequestDraw;
        for (;;) {
            synchronized (mSync) {
                if (mRequestRelease) break;
                if (mRequestSetEglContext) {
                    mRequestSetEglContext = false;
                    internalPrepare();
                }
                localRequestDraw = mRequestDraw > 0;
                if (localRequestDraw) {
                    mRequestDraw--;
//					mSync.notifyAll();
                }
            }
            if (localRequestDraw) {
                if ((mEgl != null) && mTexId >= 0) {
                    //mInputSurface.makeCurrent();
                    // clear screen with yellow color so that you can see rendering rectangle
                    GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    //mDrawer.setMatrix(mMatrix, 16);
                    //mDrawer.draw(mTexId, mMatrix);
                    //mFilter.setTextureTransformMatrix(mMatrix);
                    //mFilter.onDrawFrame(mTexId);
                    mRender.onDrawFrame(mMatrix,mTexId);
                    mInputSurface.swap();
                }
            } else {
                synchronized(mSync) {
                    try {
                        mSync.wait();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        }
        synchronized (mSync) {
            mRequestRelease = true;
            internalRelease();
            mSync.notifyAll();
        }
        if (DEBUG) Log.i(TAG, "RenderHandler thread finished:");
    }

    private final void internalPrepare() {
        if (DEBUG) Log.i(TAG, "internalPrepare:");
        internalRelease();
        mEgl = new FilterEGLBase(mShard_context, false, mIsRecordable);

        mInputSurface = mEgl.createFromSurface(mSurface);

        mInputSurface.makeCurrent();
        /*mFilter = new CameraInputFilter(mContext);
        mFilter.init();
        if(mWidth>mHeight){
            mFilter.onInputSizeChanged(mWidth,mHeight);
            mFilter.onDisplaySizeChanged(mWidth,mHeight);
        }else{
            mFilter.onInputSizeChanged(mHeight,mWidth);
            mFilter.onDisplaySizeChanged(mHeight,mWidth);
        }*/
        mRender = new BeautyRender();
        mRender.setVideoSize(mWidth,mHeight);
        mRender.setSurfaceSize(mWidth,mHeight);
        mRender.init(new BeautyFilter(mContext));
        if(mFilter != null){
            mRender.setFilter(mFilter,true);
        }
        mSurface = null;
        mSync.notifyAll();
    }

    private final void internalRelease() {
        if (DEBUG) Log.i(TAG, "internalRelease:");
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        /*if (mFilter != null) {
            mFilter.destroy();
            mFilter = null;
        }*/
        if (mEgl != null) {
            mEgl.release();
            mEgl = null;
        }
    }
    public void onBeautyChange(boolean isBeauty){
        /*if(mFilter != null){
            mFilter.onBeautyChange(isBeauty);
        }*/
        if(mRender!=null){
            mRender.onBeautyChange(isBeauty);
        }
    }
}
