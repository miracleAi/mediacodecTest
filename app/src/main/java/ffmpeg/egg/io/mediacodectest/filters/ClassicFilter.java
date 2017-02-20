package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;

/**
 * Created by zhulinping on 17/1/19.
 */

public class ClassicFilter extends MagicLookupFilter{
    public ClassicFilter(Context context) {
        this(context,"filter/lookup_classic.png");
    }
    protected ClassicFilter(Context context,String filterstr){
        super(context,filterstr);
    }
}
