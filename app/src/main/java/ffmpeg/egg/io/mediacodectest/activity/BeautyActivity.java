package ffmpeg.egg.io.mediacodectest.activity;


import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.filterrecord.view.FilterRecordView;

public class BeautyActivity extends AppCompatActivity implements View.OnClickListener {

    private FilterRecordView mFilterView;
    private int videoWidth;
    private int viddoHeight;
    private boolean isBeauty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);
        initView();
    }

    private void initView() {
        mFilterView = (FilterRecordView) findViewById(R.id.filter_view);
        findViewById(R.id.btn_camera_switch).setOnClickListener(this);
        findViewById(R.id.btn_beauty).setOnClickListener(this);
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
            case R.id.record:

                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFilterView.stopCamera();
    }
}
