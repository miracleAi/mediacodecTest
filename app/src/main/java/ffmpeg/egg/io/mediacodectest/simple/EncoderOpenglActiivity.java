package ffmpeg.egg.io.mediacodectest.simple;

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
import ffmpeg.egg.io.mediacodectest.simple.encoderopengl.DecoderToOpengl;
import ffmpeg.egg.io.mediacodectest.simple.encoderopengl.EncoderFormOpengl;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

public class EncoderOpenglActiivity extends AppCompatActivity {
    private static final int ENCODER = 1;
    private int testState = -1;
    private Button mAudioAndVideoBtn;
    private Button mAudioBtn;
    private Button mVideoBtn;
    private ExtractorToDecoder mExtractor;
    private DecoderToOpengl mDecoder;
    private EncoderFormOpengl mEncoder;
    private Runnable mAudioThread;
    private Runnable mVideoThread;
    private Runnable mAudioAndVideoThread;
    private ExecutorService mExecutor;
    private boolean mAudioEncoderDone = true;
    private boolean mVideoEncoderDone = true;
    private String mFilePath = "";

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ENCODER:
                    if (testState == 0) {
                        if (mAudioEncoderDone && mVideoEncoderDone) {
                            mAudioAndVideoBtn.setText("encoder done");
                        }
                    } else if (testState == 1) {
                        if (mAudioEncoderDone) {
                            mAudioBtn.setText("encoder done");
                        }
                    } else if (testState == 2) {
                        if (mVideoEncoderDone) {
                            mVideoBtn.setText("encoder done");
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoder_opengl_actiivity);
        mFilePath = getIntent().getStringExtra(MainActivity.PATH);
        mExecutor = Executors.newCachedThreadPool();
        initView();
    }

    public void prepareAuidoAndVideoEncoder() {
        mAudioAndVideoThread = new Runnable() {
            @Override
            public void run() {
                mExtractor = new ExtractorToDecoder(mFilePath);
                mEncoder = new EncoderFormOpengl(mExtractor, new EncoderCallback());
                mDecoder = new DecoderToOpengl(mExtractor);
                mExtractor.setmAudioDecoder(mDecoder.getmAudioDecoder());
                mExtractor.setmVideoDecoder(mDecoder.getmVideoDecoder());
                mDecoder.setmAudioEncoder(mEncoder.getmAudioEncoder());
                mDecoder.setmVideoEncoder(mEncoder.getmVideoEncoder());
                mDecoder.setInputSurface(mEncoder.getmInputSurface());
                mEncoder.setmDecoder(mDecoder);
                testState = 0;
                mEncoder.setTestState(testState);
                while (!(mVideoEncoderDone && mAudioEncoderDone)) {
                    if (!mAudioEncoderDone) {
                        mExtractor.audioExtractor();
                        mDecoder.audioDecoder();
                        mEncoder.audioEncoder();
                    }
                    if (!mVideoEncoderDone) {
                        mExtractor.videoExtractor();
                        mDecoder.videoDecoder();
                        mEncoder.videoEncoder();
                    }
                }
            }
        };
    }

    public void prepareAuidoEncoder() {
        mAudioThread = new Runnable() {
            @Override
            public void run() {
                mExtractor = new ExtractorToDecoder(mFilePath);
                mEncoder = new EncoderFormOpengl(mExtractor, new EncoderCallback());
                mDecoder = new DecoderToOpengl(mExtractor);
                mExtractor.setmAudioDecoder(mDecoder.getmAudioDecoder());
                mExtractor.setmVideoDecoder(mDecoder.getmVideoDecoder());
                mDecoder.setmAudioEncoder(mEncoder.getmAudioEncoder());
                mDecoder.setmVideoEncoder(mEncoder.getmVideoEncoder());
                mDecoder.setInputSurface(mEncoder.getmInputSurface());
                mEncoder.setmDecoder(mDecoder);
                testState = 1;
                mEncoder.setTestState(testState);
                while (!mAudioEncoderDone) {
                    mExtractor.audioExtractor();
                    mDecoder.audioDecoder();
                    mEncoder.audioEncoder();
                }
            }
        };
    }

    public void prepareVideoEncoder() {
        mVideoThread = new Runnable() {
            @Override
            public void run() {
                mExtractor = new ExtractorToDecoder(mFilePath);
                mEncoder = new EncoderFormOpengl(mExtractor, new EncoderCallback());
                mDecoder = new DecoderToOpengl(mExtractor);
                mExtractor.setmAudioDecoder(mDecoder.getmAudioDecoder());
                mExtractor.setmVideoDecoder(mDecoder.getmVideoDecoder());
                mDecoder.setmAudioEncoder(mEncoder.getmAudioEncoder());
                mDecoder.setmVideoEncoder(mEncoder.getmVideoEncoder());
                mDecoder.setInputSurface(mEncoder.getmInputSurface());
                mEncoder.setmDecoder(mDecoder);
                testState = 2;
                mEncoder.setTestState(testState);
                while (!mVideoEncoderDone) {
                    mExtractor.videoExtractor();
                    mDecoder.videoDecoder();
                    mEncoder.videoEncoder();
                }
            }
        };
    }

    private void initView() {
        mAudioAndVideoBtn = (Button) findViewById(R.id.auido_and_video);
        mAudioAndVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioEncoderDone && mVideoEncoderDone) {
                    mAudioEncoderDone = false;
                    mVideoEncoderDone = false;
                    mAudioAndVideoBtn.setText("encoding");
                    prepareAuidoAndVideoEncoder();
                    mExecutor.execute(mAudioAndVideoThread);
                }
            }
        });
        mAudioBtn = (Button) findViewById(R.id.auido_btn);
        mAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAudioEncoderDone){
                    mAudioEncoderDone = false;
                    mAudioBtn.setText("encoding");
                    prepareAuidoEncoder();
                    mExecutor.execute(mAudioThread);
                }
            }
        });
        mVideoBtn = (Button) findViewById(R.id.video_btn);
        mVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVideoEncoderDone){
                    mVideoEncoderDone = false;
                    mVideoBtn.setText("encoding");
                    prepareVideoEncoder();
                    mExecutor.execute(mVideoThread);
                }
            }
        });
    }

    class EncoderCallback implements DoneCallback {

        @Override
        public void audioDone() {
            mAudioEncoderDone = true;
            mHandler.sendMessage(mHandler.obtainMessage(ENCODER));
        }

        @Override
        public void videoDone() {
            mVideoEncoderDone = true;
            mHandler.sendMessage(mHandler.obtainMessage(ENCODER));
        }
    }
}
