package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;


/**
 * Created by zhulinping on 17/1/19.
 */

public class OceanFilter extends MagicLookupFilter{
    public OceanFilter(Context context) {
        this(context,"filter/lookup_ocean.png");
    }
    public OceanFilter(Context context,String filterstr){
        super(context,filterstr);
    }
}
