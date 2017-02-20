package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;


/**
 * Created by zhulinping on 17/1/19.
 */

public class TransferFilter extends MagicLookupFilter{
    public TransferFilter(Context context) {
        super(context,"filter/lookup_transfer.png");
    }
}
