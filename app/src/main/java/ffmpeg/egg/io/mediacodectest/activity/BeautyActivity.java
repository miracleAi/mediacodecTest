package ffmpeg.egg.io.mediacodectest.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ffmpeg.egg.io.mediacodectest.R;

public class BeautyActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_camera_switch).setOnClickListener(this);
        findViewById(R.id.btn_beauty).setOnClickListener(this);
        findViewById(R.id.record).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera_switch:
                break;
            case R.id.btn_beauty:
                break;
            case R.id.record:

                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
