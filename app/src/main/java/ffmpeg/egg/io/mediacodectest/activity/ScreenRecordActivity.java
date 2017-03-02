package ffmpeg.egg.io.mediacodectest.activity;

import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.screencast.ScreenRecorder;
import ffmpeg.egg.io.mediacodectest.screencast.ScreenRecorderTask;

public class ScreenRecordActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private MediaProjectionManager mMediaProjectionManager;
    //private ScreenRecorder mRecorder;
    private ScreenRecorderTask mRecorder;
    private ExecutorService mExecutor;
    private Button mScreenBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_record);
        mExecutor = Executors.newCachedThreadPool();
        initView();
    }

    private void initView() {
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mScreenBtn = (Button) findViewById(R.id.screen_btn);
        mScreenBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (mRecorder != null) {
                    mRecorder.quit();
                    mRecorder = null;
                    mScreenBtn.setText("Restart recorder");
                } else {
                    Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, REQUEST_CODE);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                Log.e("@@", "media projection is null");
                return;
            }
            final int width = 720;
            final int height = 1280;
            File file = new File(Environment.getExternalStorageDirectory(),
                    "record-" + width + "x" + height + "-" + System.currentTimeMillis() + ".mp4");
            final int bitrate = 6000000;
            /*mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection, file.getAbsolutePath());
            mRecorder.start();*/
            mRecorder = new ScreenRecorderTask(width, height, bitrate, 1, mediaProjection, file.getAbsolutePath());
            mExecutor.execute(mRecorder);
            mScreenBtn.setText("Stop Recorder");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecorder != null) {
            mRecorder.quit();
            mRecorder = null;
        }
    }
}
