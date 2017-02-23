package ffmpeg.egg.io.mediacodectest;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ffmpeg.egg.io.mediacodectest.activity.EditActivity;
import ffmpeg.egg.io.mediacodectest.activity.RecordActivity;
import ffmpeg.egg.io.mediacodectest.activity.ScreenRecordActivity;

public class MainActivity extends AppCompatActivity {
    public static final String PATH = "file_path";
    private Button mBeautyBtn;
    private Button mEditBtn;
    private Button mScreenBtn;
    private String mfilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mfilePath = Environment.getExternalStorageDirectory().toString()
                + "/dcim/camera/"+"VID20170222173039.mp4";
        initView();
    }

    private void initView() {
        mBeautyBtn = (Button) findViewById(R.id.beauty_btn);
        mEditBtn = (Button) findViewById(R.id.edit_btn);
        mScreenBtn = (Button) findViewById(R.id.screen_btn);
        mBeautyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
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
        mScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScreenRecordActivity.class);
                startActivity(intent);
            }
        });
    }
}
