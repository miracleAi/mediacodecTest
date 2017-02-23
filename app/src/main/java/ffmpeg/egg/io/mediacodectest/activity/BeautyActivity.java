/*
package ffmpeg.egg.io.mediacodectest.activity;

import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;

import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.filters.CombineFilter;
import ffmpeg.egg.io.mediacodectest.record.view.BeautyRecordCameraView;

public class BeautyActivity extends AppCompatActivity implements View.OnClickListener {
    private BeautyRecordCameraView cameraView;
    String savePath = "";
    private static final String DIR_NAME = "ffmpeg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);
        initView();
    }

    private void init() {
        cameraView.setPreviewResolution(1024, 576);
        cameraView.startCamera();
    }

    private void initView() {
        findViewById(R.id.btn_camera_switch).setOnClickListener(this);
        findViewById(R.id.btn_beauty).setOnClickListener(this);
        findViewById(R.id.record).setOnClickListener(this);
        cameraView = (BeautyRecordCameraView) findViewById(R.id.glsurfaceview_camera);
        init();
    }

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
                CombineFilter filter = new CombineFilter(BeautyActivity.this);
                cameraView.setFilter(filter);
                break;
            case R.id.record:

                break;
        }
    }
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
}*/
