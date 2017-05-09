package com.hgxx.whiteboard.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.InputStream;

/**
 * Created by ly on 10/12/16.
 */

public class ImageUtils {
    public enum RESIZE_MODE{
        FIT_WIDTH, FIT_HEIGHT
    }




    public static Bitmap resizeBitmap(Bitmap bm, int newWidth, int newHeight){
        int width = bm.getWidth();
        int height = bm.getHeight();

        float widthScale = (float)newWidth/width;
        float heightScale = (float)newHeight/height;

        Matrix matrix = new Matrix();

        matrix.postScale(widthScale, heightScale);
        Bitmap newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return newBm;
    }
    public static Bitmap resizeBitmapnorec(Bitmap bm, int newWidth, int newHeight){
        int width = bm.getWidth();
        int height = bm.getHeight();

        float widthScale = (float)newWidth/width;
        float heightScale = (float)newHeight/height;

        Matrix matrix = new Matrix();

        matrix.postScale(widthScale, heightScale);
        Bitmap newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return newBm;
    }

    public static Bitmap readImageFromResource(Context context, int resourceId, int width, int height, RESIZE_MODE resizeMode){

        //get original size:w
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, opt);

        //calculate scale
        int scale = calculateScale(opt.outWidth, opt.outHeight, width, height, resizeMode);

        opt.inJustDecodeBounds = false;
        opt.inSampleSize = scale;

        return BitmapFactory.decodeResource(context.getResources(), resourceId, opt);
    }

    public static Bitmap readImageFromInputStream(InputStream inputStream, int width, int height, RESIZE_MODE resizeMode){

        //get original size:w
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, opt);

        //calculate scale
        int scale = calculateScale(opt.outWidth, opt.outHeight, width, height, resizeMode);

        opt.inJustDecodeBounds = false;
        opt.inSampleSize = scale;

        return BitmapFactory.decodeStream(inputStream, null, opt);
    }

     public static Bitmap readImageFromFile(Context context, String path, int width, int height, RESIZE_MODE resizeMode){

        //get original size:w
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);

        //calculate scale
        int scale = calculateScale(opt.outWidth, opt.outHeight, width, height, resizeMode);

        opt.inJustDecodeBounds = false;
        opt.inSampleSize = scale;

        return BitmapFactory.decodeFile(path, opt);
    }

    private static int calculateScale(int srcWidth, int srcHeight, int destWidth, int destHeight, RESIZE_MODE resizeMode){
        int scale = 1;

        float mscale=1.0f;

        if(resizeMode== RESIZE_MODE.FIT_WIDTH){
            mscale = (float)srcWidth/(float)destWidth;
        }

//        int heightScale = originalHeight/height;
//        int scale = widthScale>heightScale?widthScale:heightScale;
        scale = (int)Math.ceil(mscale);
        return scale<1?1:scale;
    }


}
