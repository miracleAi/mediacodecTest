package ffmpeg.egg.io.mediacodectest.record.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ffmpeg.egg.io.mediacodectest.edit.render.FilterRender;
import ffmpeg.egg.io.mediacodectest.edit.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;
import ffmpeg.egg.io.mediacodectest.filters.MagicLookupFilter;
import ffmpeg.egg.io.mediacodectest.openglutils.OpenGlUtils;
import ffmpeg.egg.io.mediacodectest.record.encoder.MediaVideoEncoder;

/**
 * Created by zhulinping on 16/12/21.
 */

public class RecordCameraView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private Camera mCamera;
    private int mCamId = -1;
    private int mPreviewRotation = 90;
    private int mPreviewOrientation = Configuration.ORIENTATION_PORTRAIT;
    private SurfaceTexture surfaceTexture;
    private int mOESTextureId = OpenGlUtils.NO_TEXTURE;
    private int mPreviewWidth;
    private int mPreviewHeight;

    Context mContext;
    protected FilterRender mRender;
    private TranscodingResources mResources;
    private float[] mSTMatrix = new float[16];
    private int mEncoderWidth;
    private int mEncoderHeight;
    private MediaVideoEncoder mEncoder;
    private GPUImageFilter mFilter;
    public RecordCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mResources = new TranscodingResources(mContext);
        mFilter = new MagicLookupFilter(mContext, "filter/lookup_mono.png");
        //mFilter = new CombineFilter(mContext);
    }
    public RecordCameraView(Context context) {
        this(context, null);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        mResources.setSurfaceWidth(width);
        mResources.setSurfaceHeight(height);
        mResources.setVideoWidth(1080);
        mResources.setVideoHeight(1920);
        mRender = new FilterRender();
        mRender.init(mResources.getNomalFilter());
        mOESTextureId = mRender.getmGLTextureId();
        surfaceTexture = new SurfaceTexture(mOESTextureId);
        if(mFilter != null){
            mRender.setFilter(mFilter,true);
        }
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
        mResources.setSurfaceHeight(height);
        mResources.setSurfaceWidth(width);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        surfaceTexture.updateTexImage();
       /* if (mResources.getVideoRotation() == 0) {
            mSTMatrix = new ImageTransformationMatrix().adjustForTranscoderTransformation();
            //mSurfaceTexture.getTransformMatrix(mSTMatrix);
        } else if (mResources.getVideoRotation() == 90) {
            mSTMatrix = new ImageTransformationMatrix().rotateRight();
        } else if (mResources.getVideoRotation() == 180) {
            mSTMatrix = new ImageTransformationMatrix().rotate180Degrees();
        } else {
            mSTMatrix = new ImageTransformationMatrix().rotateLeft();
        }*/
        surfaceTexture.getTransformMatrix(mSTMatrix);
        mRender.onDrawFrame(mSTMatrix);
        if(mEncoder != null){
            mEncoder.frameAvailableSoon(null,mSTMatrix);
        }
    }

    public void setVideoEncoder(final MediaVideoEncoder encoder) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (encoder != null) {
                    encoder.setEglContext(mResources, EGL14.eglGetCurrentContext(), mOESTextureId);
                    if(mFilter != null){
                        encoder.setFilter(mFilter);
                    }
                    mEncoder = encoder;
                }
            }
        });
    }
    public void setFilter(GPUImageFilter filter){
        mFilter = filter;
        if(mRender != null){
            mRender.setFilter(filter,false);
        }
        if(mEncoder != null){
            mEncoder.setFilter(filter);
        }
    }
    public void setCameraId(int id) {
        mCamId = id;
    }

    public void setPreviewOrientation(int orientation) {
        mPreviewOrientation = orientation;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCamId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mPreviewRotation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? 270 : 90;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mPreviewRotation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? 180 : 0;
            }
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mPreviewRotation = 90;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mPreviewRotation = 0;
            }
        }
        mResources.setVideoRotation(mPreviewRotation);
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
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth() * 16 / 9);
    }

    public boolean startCamera() {
        setPreviewOrientation(mPreviewOrientation);
        if (mCamera == null) {
            mCamera = openCamera();
            if (mCamera == null) {
                return false;
            }
        }

        Camera.Parameters params = mCamera.getParameters();

        for (String s : params.getSupportedFocusModes()) {
            if (s.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                break;
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
        mResources.setVideoWidth(mPreviewWidth);
        mResources.setSurfaceHeight(mPreviewHeight);
        mCamera.getParameters().setPreviewSize(mPreviewWidth, mPreviewHeight);
        return new int[]{mPreviewWidth, mPreviewHeight};
    }
    public void setEncoderSize(int width, int height){
        mEncoderWidth = width;
        mEncoderHeight = height;

    }
}
