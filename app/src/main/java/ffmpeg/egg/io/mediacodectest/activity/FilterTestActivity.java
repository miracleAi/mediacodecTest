package ffmpeg.egg.io.mediacodectest.activity;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ffmpeg.egg.io.mediacodectest.MainActivity;
import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.decoder.DecoderFilter;
import ffmpeg.egg.io.mediacodectest.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.utils.VideoMetadataReader;
import ffmpeg.egg.io.mediacodectest.encoder.EncoderFilter;
import ffmpeg.egg.io.mediacodectest.extractor.ExtractorToDecoder;
import ffmpeg.egg.io.mediacodectest.filters.CombineFilter;
import ffmpeg.egg.io.mediacodectest.utils.DoneCallback;

public class FilterTestActivity extends AppCompatActivity {

    private Button mFilterEncoderBtn;
    private ExtractorToDecoder mExtractor;
    private DecoderFilter mDecoder;
    private EncoderFilter mEncoder;
    private Runnable mAudioAndVideoThread;
    private ExecutorService mExecutor;
    private boolean mAudioEncoderDone = true;
    private boolean mVideoEncoderDone = true;
    private String mFilePath = "";
    private TranscodingResources mResoreces;
    private VideoMetadataReader reader;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (mAudioEncoderDone && mVideoEncoderDone) {
                        mFilterEncoderBtn.setText("encoder done");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_test);
        mFilePath = getIntent().getStringExtra(MainActivity.PATH);
        mExecutor = Executors.newCachedThreadPool();
        init();
        initView();
    }
    private void init() {
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        mResoreces = new TranscodingResources(FilterTestActivity.this);
        reader = new VideoMetadataReader(new File(mFilePath));
        mResoreces.setVideoRotation(reader.getRotation());
        if (reader.getRotation() == 90 || reader.getRotation() == 270) {
            mResoreces.setVideoWidth(reader.getHeight());
            mResoreces.setVideoHeight(reader.getWidth());
        } else {
            mResoreces.setVideoWidth(reader.getWidth());
            mResoreces.setVideoHeight(reader.getHeight());
        }
        mResoreces.setSurfaceWidth(width);
        mResoreces.setSurfaceHeight(height);
        reader.release();
    }
    private void initView() {
        mFilterEncoderBtn = (Button) findViewById(R.id.filter_btn);
        mFilterEncoderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioEncoderDone && mVideoEncoderDone) {
                    mAudioEncoderDone = false;
                    mVideoEncoderDone = false;
                    mFilterEncoderBtn.setText("encoding");
                    prepareEncoder();
                    mExecutor.execute(mAudioAndVideoThread);
                }
            }
        });
    }

    private void prepareEncoder() {
        mAudioAndVideoThread = new Runnable() {
            @Override
            public void run() {
                mExtractor = new ExtractorToDecoder(mFilePath);
                mEncoder = new EncoderFilter(getAudioMediaFormate(),getVideoFormate(), new EncoderCallback());
                //MagicLookupFilter filter = new MagicLookupFilter(FilterTestActivity.this, "filter/lookup_mono.png");
                //BlendFilter filter = new BlendFilter(FilterTestActivity.this,"filter/shape2.png");
                CombineFilter filter = new CombineFilter(FilterTestActivity.this);
                mDecoder = new DecoderFilter(mFilePath, mResoreces, filter);
                mExtractor.setmAudioDecoder(mDecoder.getmAudioDecoder());
                mExtractor.setmVideoDecoder(mDecoder.getmVideoDecoder());
                mDecoder.setmAudioEncoder(mEncoder.getmAudioEncoder());
                mDecoder.setmVideoEncoder(mEncoder.getmVideoEncoder());
                mDecoder.setInputSurface(mEncoder.getmInputSurface());
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
    private MediaFormat getAudioMediaFormate() {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(mFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat audioFormat = null;
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                audioFormat = format;
                break;
            }
        }
        MediaFormat paramString = MediaFormat.createAudioFormat("audio/mp4a-latm",
                audioFormat.getInteger("sample-rate"),
                audioFormat.getInteger("channel-count"));
        paramString.setInteger("bitrate", 57344);
        return paramString;
    }

    private MediaFormat getVideoFormate() {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", 720, 1280);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1300000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        return format;
    }

    class EncoderCallback implements DoneCallback {

        @Override
        public void audioDone() {
            mAudioEncoderDone = true;
            mEncoder.releaseAudio();
            mDecoder.releaseAudio();
            mExtractor.releaseAudioExtractor();
            mHandler.sendMessage(mHandler.obtainMessage(0));
        }

        @Override
        public void videoDone() {
            mVideoEncoderDone = true;
            mEncoder.releaseVideo();
            mDecoder.releaseVideo();
            mExtractor.releaseVideoExtractor();
            mHandler.sendMessage(mHandler.obtainMessage(0));
        }
    }
}