package ffmpeg.egg.io.mediacodectest.record.view;

import android.hardware.Camera.Size;
import android.util.Pair;

import java.util.List;

/**
 * @author apolloyujl on 12/18/14.
 */
public class DefaultPreviewSizeAssignStrategy implements SizeAssignStrategy {

  @Override
  public Pair<Integer, Integer> assign(List<Size> sizes, int desiredW, int desiredH) {

    final double ASPECT_TOLERANCE = 0.05;
    double targetRatio = (double) desiredW / desiredH;

    if (sizes == null) {
      return null;
    }

    Pair<Integer, Integer> retval = assignExactMatch(sizes, desiredH, desiredW);
    if (retval != null) {
      return retval;
    }

    Size optimalSize = null;
    double minDiff = Double.MAX_VALUE;

    int targetHeight = desiredH;

    // Try to find an size match aspect ratio and size
    for (Size size : sizes) {
      double ratio = (double) size.height / size.width;
      if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
        continue;
      }
      if (Math.abs(size.height - targetHeight) < minDiff) {
        optimalSize = size;
        minDiff = Math.abs(size.height - targetHeight);
      }
    }

    // Cannot find the one match the aspect ratio, ignore the requirement
    if (optimalSize == null) {
      minDiff = Double.MAX_VALUE;
      for (Size size : sizes) {
        if (Math.abs(size.height - targetHeight) < minDiff) {
          optimalSize = size;
          minDiff = Math.abs(size.height - targetHeight);
        }
      }
    }
    return new Pair<Integer, Integer>(optimalSize.width, optimalSize.height);
  }

  private Pair<Integer, Integer> assignExactMatch(List<Size> sizes, int w, int h) {
    Pair<Integer, Integer> retval = null;

    for (Size s : sizes) {
      if (s.width == w && s.height == h) {
        retval = new Pair<>(w, h);
        break;
      }
    }
    return retval;
  }
}