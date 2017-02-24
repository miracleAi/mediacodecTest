package ffmpeg.egg.io.mediacodectest.activity;


import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.filterrecord.filters.LensFilterFactory;
import ffmpeg.egg.io.mediacodectest.filterrecord.view.FilterRecordView;
import ffmpeg.egg.io.mediacodectest.filters.BlendFilter;

public class BeautyActivity extends AppCompatActivity implements View.OnClickListener,
        GestureDetector.OnGestureListener {

    private FilterRecordView mFilterView;
    private int videoWidth;
    private int viddoHeight;
    private boolean isBeauty = false;
    private GestureDetector detector;
    private int filterIndex = 0;


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
                    mFilterView.setFilter(new BlendFilter(BeautyActivity.this, "filter/shape2.png"));
                }
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
                mFilterView.setFilter(LensFilterFactory.getLensFilter(BeautyActivity.this,
                        LensFilterFactory.volueOfFilter(filterIndex)));
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
                mFilterView.setFilter(LensFilterFactory.getLensFilter(BeautyActivity.this,
                        LensFilterFactory.volueOfFilter(filterIndex)));
            }
        }
        return false;
    }
}
