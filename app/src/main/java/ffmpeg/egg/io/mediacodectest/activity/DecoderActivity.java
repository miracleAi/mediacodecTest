package ffmpeg.egg.io.mediacodectest.activity;

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
import ffmpeg.egg.io.mediacodectest.decoder.AudioTrackDecoder;
import ffmpeg.egg.io.mediacodectest.decoder.VideoRenderDecoder;
import ffmpeg.egg.io.mediacodectest.extractor.AudioExtractor;
import ffmpeg.egg.io.mediacodectest.extractor.VideoExtractor;
import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;

public class DecoderActivity extends AppCompatActivity {
    private Button mPlayBtn;
    private SurfaceView mSurfaceView;
    private Runnable mDecoderThread;
    private Runnable mAudioExtractorThread;
    private Runnable mVideoExtractorThread;
    String mFilePath;
    private ExecutorService mExecutor;
    private AudioTrackDecoder mAudioDecoder;
    private VideoRenderDecoder mVideoDecoder;
    private AudioExtractor mAudioExtractor;
    private VideoExtractor mVideoExtractor;
    private boolean mAudioDecoderDone = false;
    private boolean mVideoDecoderDone = false;
    private boolean mAudioExtractorDone = false;
    private boolean mVideoExtractorDone = false;
    private boolean mAbort = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoder_play);
        mExecutor = Executors.newCachedThreadPool();
        mFilePath = getIntent().getStringExtra(MainActivity.PATH);
        mAudioExtractor = new AudioExtractor(mFilePath, new AudioExtractorDoneCallback());
        mVideoExtractor = new VideoExtractor(mFilePath, new VideoExtractorDoneCallback());
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

    private void prepare() {
        mAudioExtractorThread = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (!mAbort || !mAudioDecoderDone) {
                            if (mAudioExtractor != null && !mAudioExtractorDone) {
                                mAudioExtractor.processFrame();
                            }
                            if (mAudioDecoder != null && !mAudioDecoderDone) {
                                mAudioDecoder.processFrame();
                            }
                        } else {
                            return;
                        }
                    } catch (IllegalStateException e2) {
                        e2.printStackTrace();
                        return;
                    }
                }
            }
        };
        mVideoExtractorThread = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (!mAbort || !mVideoDecoderDone) {
                            if (mVideoExtractor != null && !mVideoExtractorDone) {
                                mVideoExtractor.processFrame();
                            }
                        } else {
                            return;
                        }
                    } catch (IllegalStateException e2) {
                        e2.printStackTrace();
                        return;
                    }
                }
            }
        };
        mDecoderThread = new Runnable() {
            @Override
            public void run() {
                render();
                //release();
            }
        };
    }
    public void render(){
        if (mAudioExtractorThread != null) {
            mExecutor.execute(mAudioExtractorThread);
        }
        if (mVideoExtractorThread != null) {
            mExecutor.execute(mVideoExtractorThread);
        }
        while (true) {
            if (!mAbort && !isConplete()) {
                if ((mVideoDecoder != null) && (!mVideoDecoderDone)) {
                    mVideoDecoder.processFrame();
                }
            } else {
                return;
            }
        }
    }

    private boolean isConplete() {
        return mAudioExtractorDone && mAudioDecoderDone
                && mVideoExtractorDone && mVideoDecoderDone;
    }

    class SurfaceViewCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mAudioDecoder = new AudioTrackDecoder(mAudioExtractor.getFormat(),
                    new AudioDecoderDoneCallback());
            mVideoDecoder = new VideoRenderDecoder(mVideoExtractor.getFormat(), holder.getSurface(),
                    new VideoDecoderDoneCallback());
            mAudioExtractor.setDecoder(mAudioDecoder.getmDecoder());
            mVideoExtractor.setDecoder(mVideoDecoder.getmDecoder());
            prepare();
            mExecutor.execute(mDecoderThread);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    class AudioExtractorDoneCallback implements StageDoneCallback {

        @Override
        public void done() {
            mAudioExtractorDone = true;
        }
    }

    class VideoExtractorDoneCallback implements StageDoneCallback {

        @Override
        public void done() {
            mVideoExtractorDone = true;
        }
    }

    class AudioDecoderDoneCallback implements StageDoneCallback {

        @Override
        public void done() {
            mAudioDecoderDone = true;
        }
    }

    class VideoDecoderDoneCallback implements StageDoneCallback {

        @Override
        public void done() {
            mVideoDecoderDone = true;
        }
    }

    public void release() {
        if (mAudioExtractor != null) {
            mAudioExtractor.release();
        }
        if (mAudioDecoder != null) {
            mAudioDecoder.release();
        }
        if (mVideoExtractor != null) {
            mVideoExtractor.release();
        }
        if (mVideoDecoder != null) {
            mVideoDecoder.release();
        }
    }

    public void abort() {
        mAbort = true;
        mAudioExtractor.abort();
        mVideoExtractor.abort();
    }

    @Override
    protected void onStop() {
        super.onStop();
        abort();
    }
}
