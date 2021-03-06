package ffmpeg.egg.io.mediacodectest.edit.task;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import ffmpeg.egg.io.mediacodectest.edit.decoder.AudioTrackDecoder;
import ffmpeg.egg.io.mediacodectest.edit.decoder.VideoFilterDecoder;
import ffmpeg.egg.io.mediacodectest.edit.extractor.AudioExtractor;
import ffmpeg.egg.io.mediacodectest.edit.extractor.VideoExtractor;
import ffmpeg.egg.io.mediacodectest.edit.surface.InputSurface;
import ffmpeg.egg.io.mediacodectest.edit.utils.StageDoneCallback;
import ffmpeg.egg.io.mediacodectest.edit.utils.TranscodingResources;
import ffmpeg.egg.io.mediacodectest.filters.BlendFilter;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;

/**
 * Created by zhulinping on 17/2/16.
 */

public class Render {
    private ExecutorService mExecutor;
    private Runnable mAudioThread;
    private Runnable mVideoThread;
    private AudioExtractor mAudioExtractor;
    private VideoExtractor mVideoExtractor;
    private AudioTrackDecoder mAudioDecoder;
    private VideoFilterDecoder mVideoDecoder;
    private boolean isRestarting = false;
    private boolean mAbort = false;
    private boolean isChangeFilter = false;
    private GPUImageFilter mFilter;
    TranscodingResources mResources;
    private boolean mAudioExtractorDone = false;
    private boolean mVideoExtractorDone = false;
    private boolean mAudioDecodeerDone = false;
    private boolean mVideoDecoderDone  = false;
    int filterIndex = -1;

    public Render(InputSurface surface, String path, TranscodingResources resources) {
        mExecutor = Executors.newCachedThreadPool();
        surface.makeCurrent();
        mResources = resources;
        mAudioExtractor = new AudioExtractor(path,new AudioExtractorDone());
        mVideoExtractor = new VideoExtractor(path,new VideoExtractorDone());
        mAudioDecoder = new AudioTrackDecoder(mAudioExtractor.getFormat(),new AudioDecoderDone());
        mVideoDecoder = new VideoFilterDecoder(resources,mVideoExtractor.getFormat(),new VideoDecoderDone());
        mAudioExtractor.setDecoder(mAudioDecoder.getmDecoder());
        mVideoExtractor.setDecoder(mVideoDecoder.getmVideoDecoder());
        mVideoDecoder.setmInputSurface(surface);
    }
    public void prepareThread(){
        mAudioThread = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (!mAbort || !mAudioDecodeerDone) {
                            if (mAudioExtractor != null && !mAudioExtractorDone) {
                                mAudioExtractor.processFrame();
                            }
                            if (mAudioDecoder != null && !mAudioDecodeerDone) {
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
        mVideoThread = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (!mAbort || !mVideoDecoderDone) {
                            if (mVideoExtractor != null && !mVideoExtractorDone) {
                                mVideoExtractor.processFrame();
                            }
                            if (mVideoExtractor != null && mVideoDecoderDone && !mAbort) {
                                mVideoExtractor.restart();
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
    }
    public void render() {
        if (mAudioThread != null) {
            mExecutor.execute(mAudioThread);
        }
        if (mVideoThread != null) {
            mExecutor.execute(mVideoThread);
        }
        while (true) {
            //updateFilters();
            if (!mAbort || !isConplete()){
                if ((mVideoDecoder != null) && (!mVideoDecoderDone)) {
                    mVideoDecoder.videoDecoder();
                }
                if (!(!isConplete() || this.mAbort || isRestarting)) {
                    restartRender();
                    this.isRestarting = true;
                }
                if (this.mVideoExtractorDone && this.mVideoDecoder != null) {
                    this.mVideoDecoder.addImediately();
                }
            } else {
                return;
            }
        }
    }


    private void restartRender() {
        this.mExecutor.execute(new FutureTask(new Callable<Void>() {
            public Void call() {
                try {
                    if (mAudioExtractor != null) {
                        mAudioExtractor.restart();
                    }
                    if(mAudioDecoder != null){
                        mAudioDecoder.restart();
                    }
                    if(mVideoDecoder != null){
                        mVideoDecoder.restart();
                    }
                    mAudioExtractorDone = false;
                    mAudioDecodeerDone = false;
                    mVideoExtractorDone = false;
                    mVideoDecoderDone = false;
                    isRestarting = false;
                    return null;
                } catch (Throwable e) {
                    throw new RuntimeException("restart faid",e);
                }
            }
        }));
    }

    private void updateFilters() {
        filterIndex = (filterIndex+1)%6 + 1;
        BlendFilter filter = new BlendFilter(mResources.getmContext(),"filter/zshape"+filterIndex+".png");
        mVideoDecoder.setFilter(filter);
        /*if (isChangeFilter) {
            mVideoDecoder.setFilter(mFilter);
            isChangeFilter = false;
        }*/
    }

    public void setFilter(GPUImageFilter filter) {
        isChangeFilter = true;
        mFilter = filter;
    }
    public void setSilence(boolean isSilence){
        mAudioDecoder.setVolume(isSilence);
    }

    public void release() {
        if (mAudioExtractor != null) {
            mAudioExtractor.release();
        }
        if(mAudioDecoder != null){
            mAudioDecoder.release();
        }
        if (mVideoExtractor != null) {
            mVideoExtractor.release();
        }
        if(mVideoDecoder != null){
            mVideoDecoder.release();
        }
    }

    private boolean isConplete() {
        return mAudioExtractorDone && mAudioDecodeerDone
                && mVideoExtractorDone && mVideoDecoderDone;
    }

    public void abort() {
        mAbort = true;
    }
    class AudioExtractorDone implements StageDoneCallback{

        @Override
        public void done() {
            mAudioExtractorDone = true;
            Log.d("mytest", "auido extractor done");
        }
    }
    class VideoExtractorDone implements StageDoneCallback{

        @Override
        public void done() {
            mVideoExtractorDone = true;
            Log.d("mytest", "video extractor done");
        }
    }
    class AudioDecoderDone implements StageDoneCallback{

        @Override
        public void done() {
            mAudioDecodeerDone = true;
            Log.d("mytest", "auido decoder done");
        }
    }
    class VideoDecoderDone implements StageDoneCallback {

        @Override
        public void done() {
            mVideoDecoderDone = true;
            Log.d("mytest", "video decoder done");

        }
    }
}

