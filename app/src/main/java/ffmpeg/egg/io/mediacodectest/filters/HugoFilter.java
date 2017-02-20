package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;

/**
 * Created by zhulinping on 17/1/19.
 */

public class HugoFilter extends MagicLookupFilter{
    public HugoFilter(Context context) {
        this(context,"filter/lookup_hugo.png");
    }
    public HugoFilter(Context context,String filterstr){
        super(context,filterstr);
    }
}
