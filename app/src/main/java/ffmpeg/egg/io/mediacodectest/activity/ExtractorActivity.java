package ffmpeg.egg.io.mediacodectest.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ffmpeg.egg.io.mediacodectest.MainActivity;
import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.extractor.ExtractorSave;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

public class ExtractorActivity extends AppCompatActivity {
    public static final int AUDIO_TYPE = 0;
    public static final int VIDEO_TYPE = 1;
    public static final int ALL_TYPE = 2;
    private Button mAudioBtn;
    private Button mVideoBtn;
    private Button mExtractorBtn;
    private static final int EXTRACTOR = 0;
    boolean mAudioExtractorDone = false;
    boolean mVideoExtractorDone = false;
    String mFilePath = "";
    private ExecutorService mExecutor;
    private Runnable mAudioThread;
    private Runnable mVideoThread;
    private Runnable mExtractorThread;
    private ExtractorSave mExtractor;
    private boolean mExtractoring = false;
    private int mType = -1;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EXTRACTOR:
                    if(mType == ALL_TYPE){
                        if(mAudioExtractorDone && mVideoExtractorDone) {
                            mExtractoring = false;
                            mExtractorBtn.setText("extractor done");
                        }
                    }else if(mType == AUDIO_TYPE){
                        if(mAudioExtractorDone){
                            mExtractoring = false;
                            mAudioBtn.setText("auido extractor done");
                        }
                    }else {
                        if (mVideoExtractorDone) {
                            mExtractoring = false;
                            mVideoBtn.setText("video extractor done");
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extractor);
        mFilePath = getIntent().getStringExtra(MainActivity.PATH);
        mExecutor = Executors.newCachedThreadPool();
        initView();
    }

    private void initView() {
        mExtractorBtn = (Button) findViewById(R.id.extractor_btn);
        mExtractorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = ALL_TYPE;
                mExtractor = new ExtractorSave(mFilePath, ALL_TYPE,new ExtractorCallback());
                if (!mExtractoring) {
                    mExtractoring = true;
                    mAudioExtractorDone = false;
                    mVideoExtractorDone = false;
                    mExtractorBtn.setText("extractoring……");
                    prepareEtractor();
                    extractor();
                }

            }
        });
        mAudioBtn = (Button) findViewById(R.id.extractor_audio_btn);
        mAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = AUDIO_TYPE;
                mExtractor = new ExtractorSave(mFilePath, AUDIO_TYPE,new ExtractorCallback());
                if(!mExtractoring){
                    mExtractoring = true;
                    mAudioExtractorDone = false;
                    mAudioBtn.setText("audio extractoring……");
                    prepareAudio();
                    extractorAudio();
                }
            }
        });
        mVideoBtn = (Button) findViewById(R.id.extractor_video_btn);
        mVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = VIDEO_TYPE;
                mExtractor = new ExtractorSave(mFilePath, VIDEO_TYPE,new ExtractorCallback());
                if(!mExtractoring){
                    mExtractoring = true;
                    mVideoExtractorDone = false;
                    mVideoBtn.setText("video extractoring……");
                    prepareVedio();
                    extractorVideo();
                }
            }
        });
    }
    public void prepareAudio() {
        mAudioThread = new Runnable() {
            @Override
            public void run() {
                mExtractor.extractorAudio();
            }
        };
    }
    public void prepareVedio() {
        mVideoThread = new Runnable() {
            @Override
            public void run() {
                mExtractor.extractorVideo();
            }
        };
    }
    public void prepareEtractor(){
        mExtractorThread = new Runnable() {
            @Override
            public void run() {
                mExtractor.extractorAudio();
                mExtractor.extractorVideo();
            }
        };
    }
    private void extractorAudio(){
        mExtractor.addAudioTrack();
        mExtractor.extractorAudio();
    }
    private void extractorVideo(){
        mExtractor.addVideoTrack();
        mExtractor.extractorVideo();
    }
    private void extractor() {
        mExtractor.addAllTrack();
        mExecutor.execute(mExtractorThread);
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
