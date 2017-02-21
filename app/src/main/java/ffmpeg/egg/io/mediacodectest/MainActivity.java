package ffmpeg.egg.io.mediacodectest;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ffmpeg.egg.io.mediacodectest.activity.BeautyActivity;
import ffmpeg.egg.io.mediacodectest.activity.EditActivity;

public class MainActivity extends AppCompatActivity {
    public static final String PATH = "file_path";
    private Button mBeautyBtn;
    private Button mEditBtn;
    private String mfilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mfilePath = Environment.getExternalStorageDirectory().toString()
                + "/dcim/camera/"+"VID_20170221_113401.mp4";
        initView();
    }

    private void initView() {
        mBeautyBtn = (Button) findViewById(R.id.beauty_btn);
        mEditBtn = (Button) findViewById(R.id.edit_btn);
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
