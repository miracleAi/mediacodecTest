package ffmpeg.egg.io.mediacodectest.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import ffmpeg.egg.io.mediacodectest.R;

public class EncoderActivity extends AppCompatActivity {
    private Button mEncoderBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoder);
    }
}
