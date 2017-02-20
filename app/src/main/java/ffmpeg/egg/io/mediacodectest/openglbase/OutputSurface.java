package ffmpeg.egg.io.mediacodectest.openglbase;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

/**
 * Created by zhulinping on 17/2/8.
 */

public class OutputSurface implements SurfaceTexture.OnFrameAvailableListener{
    private static final String TAG = "OutputSurface";
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private TextureRender mTextureRender;
    private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
    private boolean mFrameAvailable;


    public OutputSurface(){
        setup();
    }
    private void setup() {
        mTextureRender = new TextureRender();
        mTextureRender.surfaceCreated();
        mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);
    }
    public Surface getmSurface(){
        return  mSurface;
    }
    public void awaitNewImage() {
        final int TIMEOUT_MS = 500;
        synchronized (mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    mFrameSyncObject.wait(TIMEOUT_MS);
                    if (!mFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("Surface frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            mFrameAvailable = false;
        }
        // Latch the data.
        mTextureRender.checkGlError("before updateTexImage");
        mSurfaceTexture.updateTexImage();
    }
    public void changeFragmentShader(String fragmentShader) {
        mTextureRender.changeFragmentShader(fragmentShader);
    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */
    public void drawImage() {
        mTextureRender.drawFrame(mSurfaceTexture);
    }
    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        Log.d(TAG, "new frame available");
        synchronized (mFrameSyncObject) {
            if (mFrameAvailable) {
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            mFrameAvailable = true;
            mFrameSyncObject.notifyAll();
        }
    }

}
