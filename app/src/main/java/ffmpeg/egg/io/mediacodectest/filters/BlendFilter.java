package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import ffmpeg.egg.io.mediacodectest.R;
import ffmpeg.egg.io.mediacodectest.openglutils.OpenGlUtils;
import ffmpeg.egg.io.mediacodectest.openglutils.Rotation;
import ffmpeg.egg.io.mediacodectest.openglutils.TextureRotationUtil;

/**
 * Created by zhulinping on 17/1/18.
 */

public class BlendFilter extends GPUImageFilter {
    private static final String VERTEX_SHADER = "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "attribute vec4 inputTextureCoordinate2;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 textureCoordinate2;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "    textureCoordinate2 = vec2(inputTextureCoordinate2.x, 1.0 - inputTextureCoordinate2.y);\n" +
            "}";

    public int mFilterInputTextureUniform2;
    public int mFilterSourceTexture2 = OpenGlUtils.NO_TEXTURE;
    protected FloatBuffer mGLTextureBuffer2;

    protected int mGLAttribTextureCoordinate2;
    public String mBitmapStr;
    public Context mContext;

    public BlendFilter(Context context,String bitmapStr) {
        super(VERTEX_SHADER, OpenGlUtils.readShaderFromRawResource(context, R.raw.blend));
        mContext = context;
        mBitmapStr = bitmapStr;
        mGLTextureBuffer2 = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer2.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)).position(0);
    }

    @Override
    protected void onInit() {
        super.onInit();
        mGLAttribTextureCoordinate2 = GLES20.glGetAttribLocation(getProgram(), "inputTextureCoordinate2");
        mFilterInputTextureUniform2 = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture2"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate2);

    }

    protected void onInitialized() {
        super.onInitialized();
        runOnDraw(new Runnable() {
            public void run() {

                    mFilterSourceTexture2 = OpenGlUtils.loadTextureByStr(mContext,mBitmapStr);
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(1, new int[]{
                mFilterSourceTexture2
        }, 0);
        mFilterSourceTexture2 = OpenGlUtils.NO_TEXTURE;
    }

    @Override
    protected void onDrawArraysAfter() {
        super.onDrawArraysAfter();
        if (mFilterSourceTexture2 != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }
    }

    @Override
    protected void onDrawArraysPre() {
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate2);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterSourceTexture2);
        GLES20.glUniform1i(mFilterInputTextureUniform2, 3);
        mGLTextureBuffer2.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate2, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer2);


    }

}
