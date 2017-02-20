package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;


/**
 * Created by zhulinping on 17/1/19.
 */

public class SoftFilter extends MagicLookupFilter{
    public SoftFilter(Context context) {
        this(context,"filter/lookup_soft.png");
    }
    public SoftFilter(Context context,String filterstr){
        super(context,filterstr);
    }
}
