package ffmpeg.egg.io.mediacodectest.filterrecord.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ffmpeg.egg.io.mediacodectest.filterrecord.encoder.FilterMediaEncoder;
import ffmpeg.egg.io.mediacodectest.filterrecord.encoder.FilterVideoEncoder;
import ffmpeg.egg.io.mediacodectest.filterrecord.filters.BeautyFilter;
import ffmpeg.egg.io.mediacodectest.filterrecord.filters.BeautyRender;
import ffmpeg.egg.io.mediacodectest.filterrecord.filters.LensFilterFactory;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;
import ffmpeg.egg.io.mediacodectest.openglutils.OpenGlUtils;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaVideoEncoder;

/**
 * Created by zhulinping on 16/12/21.
 */

public class FilterRecordView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private Camera mCamera;
    private int mCamId = -1;
    private int mPreviewRotation = 90;
    private int mPreviewOrientation = Configuration.ORIENTATION_PORTRAIT;
    private SurfaceTexture surfaceTexture;
    private int mOESTextureId = OpenGlUtils.NO_TEXTURE;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private float mInputAspectRatio;
    private float mOutputAspectRatio;
    private BeautyRender mRender;
    Context mContext;
    private float[] mProjectionMatrix = new float[16];
    private float[] mSurfaceMatrix = new float[16];
    private float[] mTransformMatrix = new float[16];

    private FilterVideoEncoder mEncoder;
    private boolean mIsBeauty = false;
    private int mFilterIndex = 0;

    public FilterRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public FilterRecordView(Context context) {
        this(context, null);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        mRender = new BeautyRender();
        mRender.setVideoSize(mPreviewWidth,mPreviewHeight);
        mRender.init(new BeautyFilter(mContext));
        mOESTextureId = mRender.getmGLTextureId();
        surfaceTexture = new SurfaceTexture(mOESTextureId);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });

        // For camera preview on activity creation
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        mRender.setSurfaceSize(mSurfaceWidth,mSurfaceHeight);

        mOutputAspectRatio = width > height ? (float) width / height : (float) height / width;
        float aspectRatio = mOutputAspectRatio / mInputAspectRatio;
        if (width > height) {
            Matrix.orthoM(mProjectionMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1.0f, 1.0f);
        } else {
            Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mSurfaceMatrix);
        Matrix.multiplyMM(mTransformMatrix, 0, mSurfaceMatrix, 0, mProjectionMatrix, 0);
        mRender.onDrawFrame(mTransformMatrix);
        if (mEncoder != null) {
            mEncoder.frameAvailableSoon(null, mTransformMatrix);
        }
    }

    public void setCameraId(int id) {
        mCamId = id;
        setPreviewOrientation(mPreviewOrientation);
    }

    public void setPreviewOrientation(int orientation) {
        mPreviewOrientation = orientation;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCamId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                //mPreviewRotation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? 270 : 90;
                mPreviewRotation = 90;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //mPreviewRotation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? 180 : 0;
                mPreviewRotation = 0;
            }
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mPreviewRotation = 90;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mPreviewRotation = 0;
            }
        }
    }

    public int getCameraId() {
        return mCamId;
    }

    private Camera openCamera() {
        Camera camera;
        if (mCamId < 0) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int numCameras = Camera.getNumberOfCameras();
            int frontCamId = -1;
            int backCamId = -1;
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    backCamId = i;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    frontCamId = i;
                    break;
                }
            }
            if (backCamId != -1) {
                mCamId = backCamId;
            } else if (frontCamId != -1) {
                mCamId = frontCamId;
            } else {
                mCamId = 0;
            }
        }
        camera = Camera.open(mCamId);
        return camera;
    }

    private Camera.Size adaptPreviewResolution(Camera.Size resolution) {
        float diff = 100f;
        float xdy = (float) resolution.width / (float) resolution.height;
        Camera.Size best = null;
        for (Camera.Size size : mCamera.getParameters().getSupportedPreviewSizes()) {
            if (size.equals(resolution)) {
                return size;
            }
            float tmp = Math.abs(((float) size.width / (float) size.height) - xdy);
            if (tmp < diff) {
                diff = tmp;
                best = size;
            }
        }
        return best;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //setMeasuredDimension(getMeasuredHeight() * 9 / 16, getMeasuredHeight());
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth() * 16 / 9);
    }

    public boolean startCamera() {
        if (mCamera == null) {
            mCamera = openCamera();
            if (mCamera == null) {
                return false;
            }
        }

        Camera.Parameters params = mCamera.getParameters();

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (!supportedFocusModes.isEmpty()) {
            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else {
                params.setFocusMode(supportedFocusModes.get(0));
            }
        }
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Pair<Integer, Integer> optimalSize = getOptimalPreviewSize(sizes, mPreviewWidth, mPreviewHeight);
        params.setPreviewSize(optimalSize.first, optimalSize.second);
        int[] range = adaptFpsRange(24, params.getSupportedPreviewFpsRange());
        params.setPreviewFpsRange(range[0], range[1]);
        params.setPreviewFormat(ImageFormat.NV21);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        /*if (!params.getSupportedFocusModes().isEmpty()) {
            params.setFocusMode(params.getSupportedFocusModes().get(0));
        }*/
        mCamera.setParameters(params);

        mCamera.setDisplayOrientation(mPreviewRotation);

        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();

        return true;
    }

    private static Pair<Integer, Integer> getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        return new DefaultPreviewSizeAssignStrategy().assign(sizes, w, h);
    }

    private int[] adaptFpsRange(int expectedFps, List<int[]> fpsRanges) {
        expectedFps *= 1000;
        int[] closestRange = fpsRanges.get(0);
        int measure = Math.abs(closestRange[0] - expectedFps) + Math.abs(closestRange[1] - expectedFps);
        for (int[] range : fpsRanges) {
            if (range[0] <= expectedFps && range[1] >= expectedFps) {
                int curMeasure = Math.abs(range[0] - expectedFps) + Math.abs(range[1] - expectedFps);
                if (curMeasure < measure) {
                    closestRange = range;
                    measure = curMeasure;
                }
            }
        }
        return closestRange;
    }

    public void stopCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mFilterIndex = 0;
    }
    private static final boolean DEBUG = false; // TODO set false on release
    private static final String TAG = "CameraGLView";

    public void setVideoEncoder(final FilterVideoEncoder encoder) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (encoder != null) {
                    encoder.setEglContext(EGL14.eglGetCurrentContext(), mOESTextureId);
                }
                mEncoder = encoder;
                if (mEncoder != null) {
                    mEncoder.onBeautyChange(mIsBeauty);
                }
            }
        });
    }


    public int[] setPreviewResolution(int width, int height) {
        getHolder().setFixedSize(width, height);

        mCamera = openCamera();
        mPreviewWidth = width;
        mPreviewHeight = height;
        Camera.Size rs = adaptPreviewResolution(mCamera.new Size(width, height));
        if (rs != null) {
            mPreviewWidth = rs.width;
            mPreviewHeight = rs.height;
        }
        mCamera.getParameters().setPreviewSize(mPreviewWidth, mPreviewHeight);

        mInputAspectRatio = mPreviewWidth > mPreviewHeight ?
                (float) mPreviewWidth / mPreviewHeight : (float) mPreviewHeight / mPreviewWidth;

        return new int[]{mPreviewWidth, mPreviewHeight};
    }
    public void setFilter(final int index){
        mFilterIndex = index;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if(mRender != null){
                    mRender.setFilter(LensFilterFactory.getLensFilter(mContext,
                            LensFilterFactory.volueOfFilter(index)),false);
                }
            }
        });
    }
    public void onBeautyChange(boolean isBeauty) {
        mIsBeauty = isBeauty;
        mRender.onBeautyChange(isBeauty);
        if (mEncoder != null) {
            mEncoder.onBeautyChange(isBeauty);
        }
    }

}
