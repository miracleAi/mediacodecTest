package ffmpeg.egg.io.mediacodectest.activity;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaAudioEncoder;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaEncoder;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaMuxerWrapper;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaVideoEncoder;
import ffmpeg.egg.io.mediacodectest.recordold.view.RecordCameraView;


public class RecordActivity extends AppCompatActivity {
    private static final String DIR_NAME = "ffmpeg";

    private RecordCameraView cameraView;
    private MediaMuxerWrapper mMuxer;
    int videoWidth;
    int viddoHeight;
    String savePath = "";
    private boolean isBeauty = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_record);
        initView();

    }

    private void init() {
        if (videoWidth > viddoHeight) {
            cameraView.setPreviewResolution(videoWidth, viddoHeight);
        } else {
            cameraView.setPreviewResolution(viddoHeight, videoWidth);
        }
        cameraView.startCamera();
    }

    private void initView() {
        findViewById(R.id.btn_camera_switch).setOnClickListener(btn_listener);
        findViewById(R.id.btn_beauty).setOnClickListener(btn_listener);
        findViewById(R.id.record).setOnClickListener(btn_listener);
        cameraView = (RecordCameraView) findViewById(R.id.glsurfaceview_camera);
        videoWidth = 1280;
        viddoHeight = 720;
        Log.d("mytest", "width-" + videoWidth + "height-" + viddoHeight);
        init();
    }

    private View.OnClickListener btn_listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_camera_switch:
                    if (Camera.getNumberOfCameras() > 0) {
                        cameraView.setCameraId((cameraView.getCameraId() + 1) % Camera.getNumberOfCameras());
                        cameraView.stopCamera();
                        cameraView.startCamera();
                    }
                    break;
                case R.id.btn_beauty:
                    isBeauty = !isBeauty;
                    cameraView.onBeautyChange(isBeauty);
                    break;
                case R.id.record:
                    if (mMuxer == null) {
                        startRecording();
                    } else {
                        stopRecording();
                    }
                    break;
            }
        }
    };

    /**
     * start resorcing
     * This is a sample project and call this on UI thread to avoid being complicated
     * but basically this should be called on private thread because prepareing
     * of encoder is heavy work
     */
    private void startRecording() {
        if (DEBUG) Log.v(TAG, "startRecording:");
        try {
            savePath = getCaptureFile(Environment.DIRECTORY_MOVIES, ".mp4").toString();
            mMuxer = new MediaMuxerWrapper(savePath);    // if you record audio only, ".m4a" is also OK.
            if (true) {
                // for video capturing
                new MediaVideoEncoder(RecordActivity.this, mMuxer, mMediaEncoderListener, 480, 854);
            }
            if (true) {
                // for audio capturing
                new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            }
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    /**
     * request stop recording
     */
    private void stopRecording() {
        if (DEBUG) Log.v(TAG, "stopRecording:mMuxer=" + mMuxer);
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
            // you should not wait here
        }
    }

    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = "CameraFragment";

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onPrepared:encoder=" + encoder);
            if (encoder instanceof MediaVideoEncoder)
                cameraView.setVideoEncoder((MediaVideoEncoder) encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onStopped:encoder=" + encoder);
            if (encoder instanceof MediaVideoEncoder) {
                cameraView.setVideoEncoder(null);
                cameraView.stopCamera();
                if (savePath != null && !"".equals(savePath)) {
                    Intent intent = new Intent(RecordActivity.this, EncoderActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

        }
    };

    public File getCaptureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type), DIR_NAME);
        dir.mkdirs();
        if (dir.canWrite()) {
            return new File(dir, System.currentTimeMillis() + ext);
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraView.stopCamera();
    }
}
