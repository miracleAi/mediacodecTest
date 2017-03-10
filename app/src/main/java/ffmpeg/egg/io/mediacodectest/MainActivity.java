package ffmpeg.egg.io.mediacodectest;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ffmpeg.egg.io.mediacodectest.activity.BeautyActivity;
import ffmpeg.egg.io.mediacodectest.activity.DecoderPlayActivity;
import ffmpeg.egg.io.mediacodectest.activity.EncoderOpenglActiivity;
import ffmpeg.egg.io.mediacodectest.activity.EncoderTestActivity;
import ffmpeg.egg.io.mediacodectest.activity.ExtractorTestActivity;
import ffmpeg.egg.io.mediacodectest.activity.FilterTestActivity;
import ffmpeg.egg.io.mediacodectest.activity.EditActivity;

public class MainActivity extends AppCompatActivity {
    public static final String PATH = "file_path";
    private Button mExtractorBtn;
    private Button mDecoderBtn;
    private Button mEncoderBtn;
    private Button mOpenglBtn;
    private Button mFilterBtn;
    private Button mBeautyBtn;
    private Button mEditBtn;
    private String mfilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mfilePath = Environment.getExternalStorageDirectory().toString()
                + "/dcim/camera/"+"VID_20161130_153028.mp4";
        initView();
    }

    private void initView() {
        mExtractorBtn = (Button) findViewById(R.id.extractor_btn);
        mDecoderBtn = (Button) findViewById(R.id.decoder_btn);
        mEncoderBtn = (Button) findViewById(R.id.encoder_btn);
        mOpenglBtn = (Button) findViewById(R.id.opengl_btn);
        mFilterBtn = (Button) findViewById(R.id.filter_btn);
        mBeautyBtn = (Button) findViewById(R.id.beauty_btn);
        mEditBtn = (Button) findViewById(R.id.edit_btn);
        mExtractorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExtractorTestActivity.class);
                intent.putExtra(PATH,mfilePath);
                startActivity(intent);
            }
        });
        mDecoderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DecoderPlayActivity.class);
                intent.putExtra(PATH,mfilePath);
                startActivity(intent);
            }
        });
        mEncoderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EncoderTestActivity.class);
                intent.putExtra(PATH,mfilePath);
                startActivity(intent);
            }
        });
        mOpenglBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EncoderOpenglActiivity.class);
                intent.putExtra(PATH,mfilePath);
                startActivity(intent);
            }
        });
        mFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FilterTestActivity.class);
                intent.putExtra(PATH,mfilePath);
                startActivity(intent);
            }
        });
        mBeautyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BeautyActivity.class);
                startActivity(intent);
            }
        });
        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(PATH,mfilePath);
                startActivity(intent);
            }
        });
    }
}
