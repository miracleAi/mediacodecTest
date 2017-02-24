package ffmpeg.egg.io.mediacodectest.filterrecord.filters;

import android.content.Context;

import ffmpeg.egg.io.mediacodectest.filters.BlendFilter;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;

/**
 * Created by zhulinping on 17/2/24.
 */

public class LensFilterFactory {
    public static GPUImageFilter getLensFilter(Context context,LensFilterType type){
        switch (type){
            case NONE:
                return null;
            case CIRCLE:
                return new BlendFilter(context,"filter/circle.png");
            case PARACHUTE:
                return new BlendFilter(context,"filter/prachute.png");
            case COUPLE:
                return new BlendFilter(context,"filter/couple.png");
            case PLUS:
                return new BlendFilter(context,"filter/plus.png");
            case HEXAGON:
                return new BlendFilter(context,"filter/hexagon.png");
            case EYE:
                return new BlendFilter(context,"filter/eye.png");
            case HAND:
                return new BlendFilter(context,"filter/hand.png");
            case HEART1:
                return new BlendFilter(context,"filter/heart1.png");
            case HEART2:
                return new BlendFilter(context,"filter/heart2.png");
            case MOON:
                return new BlendFilter(context,"filter/moon.png");
            case STAR:
                return new BlendFilter(context,"filter/star.png");
            case PRAISE:
                return new BlendFilter(context,"filter/praise.png");
            case TRIANGLE:
                return new BlendFilter(context,"filter/triangle.png");
            default:
                return null;
        }
    }
    public static LensFilterType volueOfFilter(int index){
        switch (index){
            case 0:
                return LensFilterType.NONE;
            case 1:
                return LensFilterType.CIRCLE;
            case 2:
                return LensFilterType.PARACHUTE;
            case 3:
                return LensFilterType.COUPLE;
            case 4:
                return LensFilterType.PLUS;
            case 5:
                return LensFilterType.HEXAGON;
            case 6:
                return LensFilterType.EYE;
            case 7:
                return LensFilterType.HAND;
            case 8:
                return LensFilterType.HEART1;
            case 9:
                return  LensFilterType.HEART2;
            case 10:
                return LensFilterType.MOON;
            case 11:
                return LensFilterType.STAR;
            case 12:
                return LensFilterType.PRAISE;
            case 13:
                return LensFilterType.TRIANGLE;
            default:
                return LensFilterType.NONE;
        }
    }

}
