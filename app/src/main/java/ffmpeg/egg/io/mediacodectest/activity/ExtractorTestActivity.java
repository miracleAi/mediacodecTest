package ffmpeg.egg.io.mediacodectest.activity;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ffmpeg.egg.io.mediacodectest.MainActivity;
import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.extractor.Extractor;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

public class ExtractorTestActivity extends AppCompatActivity {
    private Button mExtractorBtn;
    private static final int EXTRACTOR = 0;
    boolean mAudioExtractorDone = true;
    boolean mVideoExtractorDone = true;
    String mFilePath = "";
    private ExecutorService mExecutor;
    private Runnable mAudioThread;
    private Runnable mVideoThread;
    private Extractor mExtractor;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EXTRACTOR:
                    if(mAudioExtractorDone && mVideoExtractorDone) {
                        mExtractorBtn.setText("extractor done");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extractor_test);
        mFilePath = getIntent().getStringExtra(MainActivity.PATH);
        mExecutor = Executors.newCachedThreadPool();
        mExtractor = new Extractor(mFilePath, new ExtractorCallback());
        initView();
    }

    private void initView() {
        mExtractorBtn = (Button) findViewById(R.id.extractor_btn);
        mExtractorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioExtractorDone && mVideoExtractorDone) {
                    mAudioExtractorDone = false;
                    mVideoExtractorDone = false;
                    mExtractorBtn.setText("extractoring……");
                    prepareEtractor();
                    extractor();
                }

            }
        });
    }
    public void prepareEtractor() {
        mAudioThread = new Runnable() {
            @Override
            public void run() {
                mExtractor.extractorAudio();
            }
        };
        mVideoThread = new Runnable() {
            @Override
            public void run() {
                mExtractor.extractorVideo();
            }
        };
    }

    private void extractor() {
        mExtractor.addTrack();
        mExecutor.execute(mAudioThread);
        mExecutor.execute(mVideoThread);
    }

    class ExtractorCallback implements DoneCallback {

        @Override
        public void audioDone() {
            mAudioExtractorDone = true;
            handler.sendMessage(handler.obtainMessage(EXTRACTOR));
        }

        @Override
        public void videoDone() {
            mVideoExtractorDone = true;
            handler.sendMessage(handler.obtainMessage(EXTRACTOR));
        }
    }

}
