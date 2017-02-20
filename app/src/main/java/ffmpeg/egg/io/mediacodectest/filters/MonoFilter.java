package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;


/**
 * Created by zhulinping on 17/1/19.
 */

public class MonoFilter extends MagicLookupFilter{
    public MonoFilter(Context context) {
        this(context,"filter/lookup_mono.png");
    }
    public MonoFilter(Context context,String filterstr){
        super(context,filterstr);
    }
}
