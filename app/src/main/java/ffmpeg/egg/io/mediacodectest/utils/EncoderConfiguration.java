package ffmpeg.egg.io.mediacodectest.utils;

import android.media.MediaFormat;

public class EncoderConfiguration
{
  private final MediaFormat mFormat;
  private final String mMimeType;
  
  public EncoderConfiguration(String paramString, MediaFormat paramMediaFormat)
  {
    mMimeType = paramString;
    mFormat = paramMediaFormat;
  }
  
  public MediaFormat getFormat()
  {
    return mFormat;
  }
  
  public String getMimeType()
  {
    return mMimeType;
  }
}