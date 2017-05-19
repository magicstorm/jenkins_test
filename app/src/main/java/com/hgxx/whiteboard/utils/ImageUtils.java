package com.hgxx.whiteboard.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.InputStream;


/**
 * Created by ly on 10/12/16.
 */

public class ImageUtils {
    public interface OnSizeReadyCallBack{
        void onSizeReady(int width, int height);
    }

    public interface OnTargetReadyCallBack<T>{
        void onTargetReady(T target);
    }

    public interface OnImageLoaded<T, E>{
        void onImageLoaded(T bm , E iv);
    }



}
