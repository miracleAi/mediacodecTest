package ffmpeg.egg.io.mediacodectest.screencast;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;

import ffmpeg.egg.io.mediacodectest.edit.encoder.AudioEncoder;
import ffmpeg.egg.io.mediacodectest.edit.muxer.Muxer;
import ffmpeg.egg.io.mediacodectest.edit.utils.EncoderConfiguration;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;

/**
 * Created by zhulinping on 17/2/23.
 */

public class AudioRecorder {
    private static final String TAG = "MediaAudioEncoder";
    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 8000;    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    private static final int BIT_RATE = 57344;
    public static final int SAMPLES_PER_FRAME = 1024;    // AAC, bytes/frame/channel
    public static final int FRAMES_PER_BUFFER = 25;    // AAC, frame/buffer/sec
    private EncoderConfiguration mConfig;
    private AudioEncoder mAudioEncoder;
    private AudioRecord audioRecord = null;
    private MediaCodec mEncoder;
    private ByteBuffer mBuffer;

    public AudioRecorder(Muxer muxer) {
        final MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, 2);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
        mConfig = new EncoderConfiguration(MIME_TYPE, audioFormat);
        mAudioEncoder = new AudioEncoder(muxer, mConfig, new EncoderDone());
        mEncoder = mAudioEncoder.getCodec();
    }

    public void audioRecord() {
        if (audioRecord == null) {
            final int min_buffer_size = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
            if (buffer_size < min_buffer_size)
                buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;

            try {
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    audioRecord = null;
                    return;
                }
                mBuffer = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
                audioRecord.startRecording();
            } catch (final Exception e) {
                audioRecord = null;
            }
            return;
        }
        int readBytes;
        // read audio data from internal mic
        mBuffer.clear();
        Log.d("mytest", "audio buffer size" + mBuffer);
        readBytes = audioRecord.read(mBuffer, SAMPLES_PER_FRAME);
        Log.d("mytest", "audio read" + readBytes);
        if (readBytes > 0) {
            // set audio data to encoder
            mBuffer.position(readBytes);
            mBuffer.flip();
            audioEncodeer(mBuffer, readBytes, getPTSUs());
        }
    }

    private void audioEncodeer(ByteBuffer buffer, int length, long presentationTimeUs) {
        if (mEncoder == null) {
            return;
        }
        final ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        final int inputBufferIndex = mEncoder.dequeueInputBuffer(10000L);
        if (inputBufferIndex >= 0) {
            final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            if (buffer != null) {
                inputBuffer.put(buffer);
            }
            if (length <= 0) {
                mEncoder.queueInputBuffer(inputBufferIndex, 0, 0,
                        presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                mEncoder.queueInputBuffer(inputBufferIndex, 0, length,
                        presentationTimeUs, 0);
            }
            mAudioEncoder.processFrame();
        } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
        }
    }

    public void release() {
        if (audioRecord != null) {
            Log.d("mytest","audio stop");
            audioRecord.stop();
            audioRecord.release();
        }
        mAudioEncoder.release();
    }

    /**
     * previous presentationTimeUs for writing
     */
    private long prevOutputPTSUs = 0;

    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;
    }

    class EncoderDone implements StageDoneCallback {

        @Override
        public void done() {
            Log.d("mytest", "audio encoder done");
        }
    }
}
