package ffmpeg.egg.io.mediacodectest.activity;


import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.IOException;

import ffmpeg.egg.io.mediacodectest.MainActivity;
import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.filterrecord.encoder.FilterAudioEncoder;
import ffmpeg.egg.io.mediacodectest.filterrecord.encoder.FilterMediaEncoder;
import ffmpeg.egg.io.mediacodectest.filterrecord.encoder.FilterMuxer;
import ffmpeg.egg.io.mediacodectest.filterrecord.encoder.FilterVideoEncoder;
import ffmpeg.egg.io.mediacodectest.filterrecord.filters.BeautyFilter;
import ffmpeg.egg.io.mediacodectest.filterrecord.filters.LensFilterFactory;
import ffmpeg.egg.io.mediacodectest.filterrecord.view.FilterRecordView;
import ffmpeg.egg.io.mediacodectest.filters.BlendFilter;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaAudioEncoder;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaEncoder;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaMuxerWrapper;
import ffmpeg.egg.io.mediacodectest.recordold.encoder.MediaVideoEncoder;

public class BeautyActivity extends AppCompatActivity implements View.OnClickListener,
        GestureDetector.OnGestureListener {
    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = "CameraFragment";
    private static final String DIR_NAME = "mediacodectest";

    private FilterRecordView mFilterView;
    private int videoWidth;
    private int viddoHeight;
    private boolean isBeauty = false;
    private GestureDetector detector;
    private int filterIndex = 0;
    String savePath = "";
    private FilterMuxer mMuxer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);
        detector = new GestureDetector(this, this);
        initView();
    }

    private void initView() {
        mFilterView = (FilterRecordView) findViewById(R.id.filter_view);
        findViewById(R.id.btn_camera_switch).setOnClickListener(this);
        findViewById(R.id.btn_beauty).setOnClickListener(this);
        findViewById(R.id.filter_btn).setOnClickListener(this);
        findViewById(R.id.record).setOnClickListener(this);
        videoWidth = 1280;
        viddoHeight = 720;
        init();
    }

    private void init() {
        if (videoWidth > viddoHeight) {
            mFilterView.setPreviewResolution(videoWidth, viddoHeight);
        } else {
            mFilterView.setPreviewResolution(viddoHeight, videoWidth);
        }
        mFilterView.startCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera_switch:
                if (Camera.getNumberOfCameras() > 0) {
                    mFilterView.setCameraId((mFilterView.getCameraId() + 1) % Camera.getNumberOfCameras());
                    mFilterView.stopCamera();
                    mFilterView.startCamera();
                }
                break;
            case R.id.btn_beauty:
                isBeauty = !isBeauty;
                mFilterView.onBeautyChange(isBeauty);
                break;
            case R.id.filter_btn:
                if (mFilterView != null) {
                    mFilterView.setFilter(filterIndex);
                }
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

    @Override
    protected void onStop() {
        super.onStop();
        mFilterView.stopCamera();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e2.getX() - e1.getX() > 0) {
            if (filterIndex == 0) {
                filterIndex = 13;
            } else {
                filterIndex = filterIndex - 1;
            }
            if(mFilterView != null){
                mFilterView.setFilter(filterIndex);
            }
        }
        if (e1.getX() - e2.getX() > 0) {
            Log.d("fling", "call left");
            if (filterIndex == 13) {
                filterIndex = 0;
            } else {
                filterIndex = filterIndex + 1;
            }
            if(mFilterView != null){
                mFilterView.setFilter(filterIndex);
            }
        }
        return false;
    }
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
            mMuxer = new FilterMuxer(savePath);    // if you record audio only, ".m4a" is also OK.
            if (true) {
                // for video capturing
                new FilterVideoEncoder(BeautyActivity.this, mMuxer, mMediaEncoderListener, 480, 854);
            }
            if (true) {
                // for audio capturing
                new FilterAudioEncoder(mMuxer, mMediaEncoderListener);
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
    /**
     * callback methods from encoder
     */
    private final FilterMediaEncoder.MediaEncoderListener mMediaEncoderListener = new FilterMediaEncoder.MediaEncoderListener() {

        @Override
        public void onPrepared(FilterMediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onPrepared:encoder=" + encoder);
            if (encoder instanceof FilterVideoEncoder)
                mFilterView.setVideoEncoder((FilterVideoEncoder) encoder);
        }

        @Override
        public void onStopped(FilterMediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onStopped:encoder=" + encoder);
            if (encoder instanceof FilterVideoEncoder) {
                mFilterView.setVideoEncoder(null);
                mFilterView.stopCamera();
                if (savePath != null && !"".equals(savePath)) {
                    Intent intent = new Intent(BeautyActivity.this, EditActivity.class);
                    intent.putExtra(MainActivity.PATH,savePath);
                    startActivity(intent);
                    finish();
                }
            }

        }
    };
    public File getCaptureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type), DIR_NAME);
        Log.d(TAG, "path=" + dir.toString());
        dir.mkdirs();
        if (dir.canWrite()) {
            return new File(dir, System.currentTimeMillis() + ext);
        }
        return null;
    }
}
