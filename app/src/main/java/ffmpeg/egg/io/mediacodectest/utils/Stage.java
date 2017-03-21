package ffmpeg.egg.io.mediacodectest.utils;

import ffmpeg.egg.io.mediacodectest.utils.StageDoneCallback;

public abstract class Stage
{
  protected static final String TAG = "STAGE";
  protected static final int kTIMEOUT_USEC = 10000;
  public boolean mDone = false;
  protected StageDoneCallback mDoneCallback = null;
  
  protected Stage(StageDoneCallback paramStageDoneCallback)
  {
    mDoneCallback = paramStageDoneCallback;
  }
  
  public abstract void processFrame();
  
  public void stageComplete()
  {
    mDone = true;
    mDoneCallback.done();
  }
}