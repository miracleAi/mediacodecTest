package ffmpeg.egg.io.mediacodectest.recordold.view;

import android.hardware.Camera.Size;
import android.util.Pair;

import java.util.List;

/**
 * @author apolloyujl on 12/18/14.
 */
public interface SizeAssignStrategy {
  Pair<Integer, Integer> assign(List<Size> sizes, int desiredW, int desiredH);
}
