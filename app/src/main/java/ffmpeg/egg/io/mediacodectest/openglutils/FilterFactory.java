package ffmpeg.egg.io.mediacodectest.openglutils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ffmpeg.egg.io.mediacodectest.filters.BlendFilter;
import ffmpeg.egg.io.mediacodectest.filters.ClassicFilter;
import ffmpeg.egg.io.mediacodectest.filters.CombineFilter;
import ffmpeg.egg.io.mediacodectest.filters.GPUImageFilter;
import ffmpeg.egg.io.mediacodectest.filters.HugoFilter;
import ffmpeg.egg.io.mediacodectest.filters.InstantFilter;
import ffmpeg.egg.io.mediacodectest.filters.MonoFilter;
import ffmpeg.egg.io.mediacodectest.filters.OceanFilter;
import ffmpeg.egg.io.mediacodectest.filters.SoftFilter;
import ffmpeg.egg.io.mediacodectest.filters.TeaFilter;
import ffmpeg.egg.io.mediacodectest.filters.TransferFilter;
import ffmpeg.egg.io.mediacodectest.filters.WaterMarkFilter;

/**
 * Created by zhulinping on 17/2/19.
 */

public class FilterFactory {
    public static GPUImageFilter getFilter(Context context, FilterType type){
        switch (type){
            case NONE:
                return null;
            case INSTANT:
                return new InstantFilter(context);
            case MONO:
                return new MonoFilter(context);
            case TRANSFER:
                return new TransferFilter(context);
            case CLASSIC:
                return new ClassicFilter(context);
            case HUGO:
                return new HugoFilter(context);
            case OCEAN:
                return new OceanFilter(context);
            case SOFT:
                return new SoftFilter(context);
            case TEA:
                return new TeaFilter(context);
            default:
                return null;
        }
    }
    public static GPUImageFilter getWaterMarkFilter(Context context,FilterType type){
        switch (type){
            case NONE:
                return new BlendFilter(context,"filter/watermark.png");
            case INSTANT:
                List<GPUImageFilter> insFilters = new ArrayList<>();
                insFilters.add(new InstantFilter(context));
                insFilters.add(new BlendFilter(context,"filter/watermark.png"));
                return new WaterMarkFilter(context,insFilters);
            case MONO:
                List<GPUImageFilter> monoFilters = new ArrayList<>();
                monoFilters.add(new MonoFilter(context));
                monoFilters.add(new BlendFilter(context,"filter/watermark.png"));
                return new WaterMarkFilter(context,monoFilters);
            case TRANSFER:
                List<GPUImageFilter> transFilters = new ArrayList<>();
                transFilters.add(new TransferFilter(context));
                transFilters.add(new BlendFilter(context,"filter/watermark.png"));
                return new WaterMarkFilter(context,transFilters);
            case CLASSIC:
                List<GPUImageFilter> classicFilters = new ArrayList<>();
                classicFilters.add(new ClassicFilter(context));
                classicFilters.add(new BlendFilter(context,"filter/watermark.png"));
                return new WaterMarkFilter(context,classicFilters);
            case HUGO:
                List<GPUImageFilter> hugoFilters = new ArrayList<>();
                hugoFilters.add(new HugoFilter(context));
                hugoFilters.add(new BlendFilter(context,"filter/watermark.png"));
                return new WaterMarkFilter(context,hugoFilters);
            case OCEAN:
                List<GPUImageFilter> oceanFilters = new ArrayList<>();
                oceanFilters.add(new OceanFilter(context));
                oceanFilters.add(new BlendFilter(context,"filter/watermark.png"));
                return new WaterMarkFilter(context,oceanFilters);
            case SOFT:
                List<GPUImageFilter> softFilters = new ArrayList<>();
                softFilters.add(new SoftFilter(context));
                softFilters.add(new BlendFilter(context,"filter/watermark.png"));
                return new WaterMarkFilter(context,softFilters);
            case TEA:
                List<GPUImageFilter> teaFilters = new ArrayList<>();
                teaFilters.add(new TeaFilter(context));
                teaFilters.add(new BlendFilter(context,"filter/watermark.png"));
                return new WaterMarkFilter(context,teaFilters);
            default:
                return null;
        }
    }
    public static FilterType volueOfFilter(int index){
        switch (index){
            case 0:
                return FilterType.NONE;
            case 1:
                return FilterType.INSTANT;
            case 2:
                return FilterType.MONO;
            case 3:
                return FilterType.TRANSFER;
            case 4:
                return FilterType.CLASSIC;
            case 5:
                return FilterType.HUGO;
            case 6:
                return FilterType.OCEAN;
            case 7:
                return FilterType.SOFT;
            case 8:
                return FilterType.TEA;
            default:
                return  FilterType.NONE;
        }
    }
}
