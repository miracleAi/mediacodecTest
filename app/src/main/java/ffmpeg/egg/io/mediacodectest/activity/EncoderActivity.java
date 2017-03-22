package ffmpeg.egg.io.mediacodectest.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ffmpeg.egg.io.mediacodectest.MainActivity;
import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.openglutils.FilterFactory;
import ffmpeg.egg.io.mediacodectest.task.TranscoderTask;
import ffmpeg.egg.io.mediacodectest.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.utils.VideoMetadataReader;

public class EncoderActivity extends AppCompatActivity {
    public static final String ENCODE_DONE = "encode_done";
    private Button mEncoderBtn;
    String mFilePath;
    private ExecutorService mExecutor;
    private EncoderReceiver encoderReceiver;
    private TranscodingResources mResoureces;
    private VideoMetadataReader reader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoder);
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

    private void initView() {
        mEncoderBtn = (Button) findViewById(R.id.encoder_btn);
        mEncoderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String outputVideoPath = Environment.getExternalStorageDirectory().toString()
                        + "/dcim/camera/" + System.currentTimeMillis() + ".mp4";
                TranscoderTask transcoder = new TranscoderTask(mResoureces, mFilePath, outputVideoPath);
                mEncoderBtn.setText("transcoding……");
                transcoder.setmFilter(FilterFactory.getFilter(EncoderActivity.this,
                        FilterFactory.volueOfFilter(1)));
                mExecutor.execute(transcoder);
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
            mEncoderBtn.setText("transcoder done");
            Log.d("mytest", "receiver done");
        }
    }

    private void init() {
        mResoureces = new TranscodingResources(EncoderActivity.this);
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

    @Override
    protected void onStop() {
        super.onStop();
        if(encoderReceiver != null){
            unregisterReceiver(encoderReceiver);
        }
    }
}
