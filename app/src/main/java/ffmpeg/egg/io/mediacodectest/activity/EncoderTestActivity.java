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
import ffmpeg.egg.io.mediacodectest.decoder.DecoderToEncoder;
import ffmpeg.egg.io.mediacodectest.encoder.Encoder;
import ffmpeg.egg.io.mediacodectest.extractor.ExtractorToDecoder;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

/**
 * encoder 不能成功
 * colorformat不匹配
 * encoder配置的colorformat只能保存surface传来的数据
 * */

public class EncoderTestActivity extends AppCompatActivity {
    private static final int ENCODER = 2;
    private Button mEncoderBtn;
    Encoder mEncoder;
    ExtractorToDecoder mExtractor;
    DecoderToEncoder mDecoder;
    String mFilePath = "";
    private ExecutorService mExecutor;
    private Runnable mAudioThread;
    private Runnable mVideoThread;
    boolean isAudioEncoderDone = true;
    boolean isVideoEncoderDone = true;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ENCODER:
                    if(isAudioEncoderDone){
                    mEncoderBtn.setText("encoder done");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoder_test);
        mFilePath = getIntent().getStringExtra(MainActivity.PATH);
        mExecutor = Executors.newCachedThreadPool();
        mExtractor = new ExtractorToDecoder(mFilePath);
        mEncoder = new Encoder(mExtractor,new EncoderCallback());
        mDecoder = new DecoderToEncoder(mEncoder,mExtractor);
        mDecoder.setmAudioEncoder(mEncoder.getmAudioEncoder());
        mDecoder.setmVideoEncoder(mEncoder.getmVideoEncoder());
        mExtractor.setmAudioDecoder(mDecoder.getmAudioDecoder());
        mExtractor.setmVideoDecoder(mDecoder.getmVideoDecoder());
        initView();

    }

    private void initView() {
        mEncoderBtn = (Button) findViewById(R.id.encoder_btn);
        mEncoderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAudioEncoderDone && isVideoEncoderDone) {
                    isAudioEncoderDone = false;
                    isVideoEncoderDone = false;
                    mEncoderBtn.setText("encoding");
                    prepareEncoder();
                    encoder();
                }
            }
        });
    }

    public void prepareEncoder() {
        mAudioThread = new Runnable() {
            @Override
            public void run() {
                while (!isAudioEncoderDone){
                mExtractor.audioExtractor();
                mDecoder.audioDecoder();
                mEncoder.audioEncoder();
                }
            }
        };
        mVideoThread = new Runnable() {
            @Override
            public void run() {
                while (!isVideoEncoderDone){
                mExtractor.videoExtractor();
                mDecoder.videoDeocder();
                mEncoder.videoEncoder();
                }
            }
        };
    }

    public void encoder() {
        mExecutor.execute(mAudioThread);
        mExecutor.execute(mVideoThread);
    }

    class EncoderCallback implements DoneCallback {

        @Override
        public void audioDone() {
            isAudioEncoderDone = true;
            handler.sendMessage(handler.obtainMessage(ENCODER));
        }

        @Override
        public void videoDone() {
            isVideoEncoderDone = true;
            handler.sendMessage(handler.obtainMessage(ENCODER));
        }
    }
}
