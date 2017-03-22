package ffmpeg.egg.io.mediacodectest.surface;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;

/**
 * Created by zhulinping on 17/2/8.
 */

public class InputSurface {
    private Surface mSurface;
    private EGLContext mEGLContext;
    private EGLDisplay mEGLDisplay;
    private EGLSurface mEGLSurface;
    private boolean mShouldReleaseSurface = true;

    public InputSurface(Surface paramSurface) {
        this(paramSurface, true);
    }

    public InputSurface(Surface paramSurface, boolean paramBoolean) {
        if (paramSurface == null) {
            throw new NullPointerException();
        }
        mSurface = paramSurface;
        mShouldReleaseSurface = paramBoolean;
        setupEgl();
    }

    private void checkEglError(String paramString) {
        int i;
        for (i = 0; ; i = 1) {
            int j = EGL14.eglGetError();
            if (j == 12288) {
                break;
            }
            new StringBuilder().append(paramString).append(": EGL error: 0x").append(Integer.toHexString(j));
        }
        if (i != 0) {
            throw new RuntimeException("EGL error encountered (see log)");
        }
    }

    private void setupEgl() {
        mEGLDisplay = EGL14.eglGetDisplay(0);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        Object localObject = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, (int[]) localObject, 0, (int[]) localObject, 1)) {
            mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }
        localObject = new EGLConfig[1];
        int[] arrayOfInt = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay,
                new int[]{12324, 8, 12323, 8, 12322, 8, 12352, 4, 12610, 1, 12344},
                0, (EGLConfig[]) localObject, 0, 1, arrayOfInt, 0)) {
            throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
        }
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, ((EGLConfig[]) localObject)[0], EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
        checkEglError("eglCreateContext");
        if (mEGLContext == null) {
            throw new RuntimeException("null context");
        }
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, ((EGLConfig[]) localObject)[0], mSurface, new int[]{12344}, 0);
        checkEglError("eglCreateWindowSurface");
        if (mEGLSurface == null) {
            throw new RuntimeException("surface was null");
        }
    }


    public Surface getSurface() {
        return mSurface;
    }

    public void makeCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            Log.d("mytest","make current failed");
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }
    public void setPresentationTime(long paramLong)
    {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, paramLong);
    }

    public boolean swapBuffers() {
        boolean bb = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        return bb;

    }

    public void release() {
        if (EGL14.eglGetCurrentContext().equals(mEGLContext)) {
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        }
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEGLDisplay = null;
        mEGLContext = null;
        mEGLSurface = null;
        if (mShouldReleaseSurface) {
            mSurface.release();
            mShouldReleaseSurface = false;
        }
        mSurface = null;
    }
}
