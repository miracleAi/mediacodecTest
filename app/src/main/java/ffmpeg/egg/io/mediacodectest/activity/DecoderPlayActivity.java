package ffmpeg.egg.io.mediacodectest.activity;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ffmpeg.egg.io.mediacodectest.MainActivity;
import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.decoder.DecoderToPlay;
import ffmpeg.egg.io.mediacodectest.extractor.ExtractorToDecoder;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

public class DecoderPlayActivity extends AppCompatActivity {

    private Button mPlayBtn;
    private SurfaceView mSurfaceView;
    DecoderToPlay mDecoder;
    ExtractorToDecoder mExtractor;
    String mFilePath;
    private ExecutorService mExecutor;
    private Runnable mAudioThread;
    private Runnable mVideoThread;
    private boolean isAudioDecoderDone = false;
    private boolean isVideoDecoderDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoder_play);
        mExecutor = Executors.newCachedThreadPool();
        mFilePath = getIntent().getStringExtra(MainActivity.PATH);
        initView();
    }

    private void initView() {
        mPlayBtn = (Button) findViewById(R.id.play_btn);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(new SurfaceViewCallback());
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void prepare() {
        mAudioThread = new Runnable() {
            @Override
            public void run() {
                while (!isAudioDecoderDone) {
                    mExtractor.audioExtractor();
                    mDecoder.audioDecoder();
                }
            }
        };
        mVideoThread = new Runnable() {
            @Override
            public void run() {
                while (!isVideoDecoderDone){
                    mExtractor.videoExtractor();
                mDecoder.videoDecoder();
                }
            }
        };

    }

    class SurfaceViewCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mExtractor = new ExtractorToDecoder(mFilePath);
            mDecoder = new DecoderToPlay(holder.getSurface(), mExtractor,new DecoderDoneCallback());
            mExtractor.setmAudioDecoder(mDecoder.getmAudioDecoder());
            mExtractor.setmVideoDecoder(mDecoder.getmVideoDecoder());
            prepare();
            mExecutor.execute(mAudioThread);
            mExecutor.execute(mVideoThread);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    class DecoderDoneCallback implements DoneCallback {


        @Override
        public void audioDone() {
            isAudioDecoderDone = true;
        }

        @Override
        public void videoDone() {
            isVideoDecoderDone = true;
        }
    }
}
