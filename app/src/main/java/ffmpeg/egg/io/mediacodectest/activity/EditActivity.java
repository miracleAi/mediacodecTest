package ffmpeg.egg.io.mediacodectest.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ffmpeg.egg.io.mediacodectest.MainActivity;
import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.task.RenderTask;
import ffmpeg.egg.io.mediacodectest.task.TranscoderTask;
import ffmpeg.egg.io.mediacodectest.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.utils.VideoMetadataReader;
import ffmpeg.egg.io.mediacodectest.openglutils.FilterFactory;

public class EditActivity extends AppCompatActivity {
    public static final String ENCODE_DONE = "encode_done";
    private SurfaceView mSurfaceView;
    private Button mExtractorBtn;
    private Button mSilenceBen;
    String mFilePath = "";
    private ExecutorService mExecutor;
    private EncoderReceiver encoderReceiver;
    private TranscodingResources mResoureces;
    private VideoMetadataReader reader;
    private RenderTask mThread;
    private boolean isSilence = false;
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mFilePath = getIntent().getStringExtra(MainActivity.PATH);
        mExecutor = Executors.newCachedThreadPool();
        init();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initReceiver();

    }

    private void init() {
        /*WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
*/
        mResoureces = new TranscodingResources(EditActivity.this);
        reader = new VideoMetadataReader(new File(mFilePath));
        mResoureces.setVideoRotation(reader.getRotation());
        if (reader.getRotation() == 90 || reader.getRotation() == 270) {
            mResoureces.setVideoWidth(reader.getHeight());
            mResoureces.setVideoHeight(reader.getWidth());
        } else {
            mResoureces.setVideoWidth(reader.getWidth());
            mResoureces.setVideoHeight(reader.getHeight());
        }
        reader.release();
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(new SurfaceViewCallback());
        mExtractorBtn = (Button) findViewById(R.id.play_btn);
        mExtractorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String outputVideoPath = Environment.getExternalStorageDirectory().toString()
                        + "/dcim/camera/" + System.currentTimeMillis() + ".mp4";
                TranscoderTask transcoder = new TranscoderTask(mResoureces, mFilePath, outputVideoPath);
                mExtractorBtn.setText("transcoding……");
                transcoder.setmFilter(FilterFactory.getFilter(EditActivity.this,
                        FilterFactory.volueOfFilter(index%9)));
                transcoder.setSilence(isSilence);
                mExecutor.execute(transcoder);
            }
        });
        mSilenceBen = (Button) findViewById(R.id.silence_btn);
        mSilenceBen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSilence = !isSilence;
                if (isSilence) {
                    mSilenceBen.setText("true");
                } else {
                    mSilenceBen.setText("false");
                }
                index = index + 1;
                if (mThread != null) {
                    Log.d("mytest", "test silence" + isSilence);
                    mThread.setSilence(isSilence);
                    mThread.setFilter(FilterFactory.getFilter(EditActivity.this,
                            FilterFactory.volueOfFilter(index%9)));
                }
            }
        });
    }

    private void initReceiver() {
        encoderReceiver = new EncoderReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ENCODE_DONE);
        registerReceiver(encoderReceiver, filter);
    }

    class EncoderReceiver extends BroadcastReceiver

    {

        @Override
        public void onReceive(Context context, Intent intent) {
            mExtractorBtn.setText("transcoder done");
            Log.d("mytest", "receiver done");
        }
    }

    class SurfaceViewCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mThread = new RenderTask(new InputSurface(holder.getSurface()), mFilePath, mResoureces);
            mThread.setFilter(FilterFactory.getFilter(EditActivity.this,
                    FilterFactory.volueOfFilter(index%9)));
            mThread.setSilence(isSilence);
            mExecutor.execute(mThread);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mResoureces.setSurfaceWidth(width);
            mResoureces.setSurfaceHeight(height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mThread != null) {
            mThread.tryAbort();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(encoderReceiver);
    }
}
