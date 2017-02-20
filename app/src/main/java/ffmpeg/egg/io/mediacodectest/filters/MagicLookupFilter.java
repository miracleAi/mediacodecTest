package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;
import android.opengl.GLES20;

import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.openglutils.OpenGlUtils;


public class MagicLookupFilter extends GPUImageFilter {
    protected Context mContext;
	protected String mBitmapStr;

    public MagicLookupFilter(Context context, String bitmapStr) {
        super(NO_FILTER_VERTEX_SHADER, OpenGlUtils.readShaderFromRawResource(context, R.raw.single_lookup));
        this.mContext = context;
		this.mBitmapStr = bitmapStr;
    }
    
    public int mLookupTextureUniform;
    public int mLookupSourceTexture = OpenGlUtils.NO_TEXTURE;
    
    protected void onInit(){
		super.onInit();
		mLookupTextureUniform = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture2");
    }
    
    protected void onInitialized(){
		super.onInitialized();
    	runOnDraw(new Runnable(){
    		public void run(){
    			mLookupSourceTexture = OpenGlUtils.loadTextureByStr(mContext,mBitmapStr);
    		}
    	});
    }
    
    protected void onDestroy(){
		super.onDestroy();
	    int[] texture = new int[]{mLookupSourceTexture};
	    GLES20.glDeleteTextures(1, texture, 0);
	    mLookupSourceTexture = -1;
	}
	  
	protected void onDrawArraysAfter(){
		if (mLookupSourceTexture != -1){
			GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    }
	}
	  
	protected void onDrawArraysPre(){
		if (mLookupSourceTexture != -1){
			GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLookupSourceTexture);
			GLES20.glUniform1i(mLookupTextureUniform, 3);
	    }
	}
}
