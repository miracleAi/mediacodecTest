package ffmpeg.egg.io.mediacodectest.edit.utils;

import android.opengl.Matrix;

public class ImageTransformationMatrix {
    float[] mTransformation = new float[16];

    public float[] flipVertical() {
        Matrix.setIdentityM(this.mTransformation, 0);
        return this.mTransformation;
    }

    public float[] adjustForTranscoderTransformation() {
        Matrix.setRotateM(this.mTransformation, 0, 180f, 1.0f, 0.0f, 0.0f);
        Matrix.translateM(this.mTransformation, 0, 0.0f, -1.0f, 0.0f);
        return this.mTransformation;
    }

    public float[] flipHorizontal() {
        Matrix.setRotateM(this.mTransformation, 0, 180f, 0.0f, 0.0f, 1.0f);
        Matrix.translateM(this.mTransformation, 0, -1.0f, -1.0f, 0.0f);
        return this.mTransformation;
    }

    public float[] rotate180Degrees() {
        Matrix.setRotateM(this.mTransformation, 0, 180f, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(this.mTransformation, 0, -1.0f, 0.0f, 0.0f);
        return this.mTransformation;
    }

    public float[] rotateRight() {
        Matrix.rotateM(adjustForTranscoderTransformation(), 0, 90.0f, 0.0f, 0.0f, 1.0f);
        Matrix.translateM(this.mTransformation, 0, 0.0f, -1.0f, 0.0f);
        return this.mTransformation;
    }

    public float[] rotateLeft() {
        Matrix.rotateM(adjustForTranscoderTransformation(), 0, 270f, 0.0f, 0.0f, 1.0f);
        Matrix.translateM(this.mTransformation, 0, -1.0f, 0.0f, 0.0f);
        return this.mTransformation;
    }
}
