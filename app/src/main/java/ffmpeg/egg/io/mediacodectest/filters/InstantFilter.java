package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;


/**
 * Created by zhulinping on 17/1/19.
 */

public class InstantFilter extends MagicLookupFilter{
    public InstantFilter(Context context) {
        this(context,"filter/lookup_instant.png");
    }
    public InstantFilter(Context context,String filterstr){
        super(context,filterstr);
    }
}
