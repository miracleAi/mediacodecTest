package ffmpeg.egg.io.mediacodectest.filters;

import android.content.Context;

import java.util.List;

/**
 * Created by zhulinping on 17/2/20.
 */

public class WaterMarkFilter extends FilterGroup{
    Context mContext;
    public WaterMarkFilter(Context context,List<GPUImageFilter> filters) {
        super();
        mContext = context;
        setFilters(filters);
    }
}
