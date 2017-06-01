package com.hgxx.whiteboard.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hgxx.whiteboard.views.PageLoadListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


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

    public interface OnImageLoaded{
        void onImageLoaded(Bitmap bm);
        void onFailure();
    }

    public static void downloadImage(final String urlString, final OnImageLoaded onImageLoaded){
        new Thread(){
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                ByteArrayOutputStream out = null;
                BufferedInputStream in = null;

                try {
                    final URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
                    out = new ByteArrayOutputStream();

                    byte[] b = new byte[1024*8];
                    int len;
                    while ((len = in.read(b)) != -1) {
                        out.write(b, 0, len);
                        b = new byte[1024*8];
                    }
                    final byte[] resultBytes = out.toByteArray();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(onImageLoaded!=null){
                                onImageLoaded.onImageLoaded(BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.length));
                            }
                        }
                    });
                } catch (final IOException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(onImageLoaded!=null){
                                onImageLoaded.onFailure();
                            }
                        }
                    });
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }

            }



        }.start();
    }


}
