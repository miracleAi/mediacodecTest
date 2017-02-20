package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhulinping on 17/2/9.
 */

public class CombineFilter extends FilterGroup{
    Context mContext;
    public CombineFilter(Context context) {
        super();
        mContext = context;
        setFilters(initFilters());
    }
    private List<GPUImageFilter> initFilters(){
        List<GPUImageFilter> filters = new ArrayList<GPUImageFilter>();
        filters.add(new MagicLookupFilter(mContext,"filter/lookup_mono.png"));
        BlendFilter blendFilter = new BlendFilter(mContext,"filter/shape2.png");
        filters.add(blendFilter);
        return filters;
    }
}
