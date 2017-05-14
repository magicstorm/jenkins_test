package com.hgxx.whiteboard.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;

import java.io.InputStream;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.Subject;

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

    public static <T> Observable<T> getLoadImageObserve(Context context, String url, ImageView iv,
                                                        OnTargetReadyCallBack onTargetReadyCallBack, OnImageLoaded onImageLoaded, T tag){
        return getLoadImageObserve(context, url, iv, onTargetReadyCallBack, null, onImageLoaded, tag);

    }


    public static <T> Observable<T> getLoadImageObserve(final Context context, final String url, final ImageView iv,
                                                                            final OnTargetReadyCallBack onTargetReadyCallBack, final OnSizeReadyCallBack onSizeReadyCallBack, final OnImageLoaded onImageLoaded, final T tag){

        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                SimpleTarget target = Glide.with(context)
                        .load(url).asBitmap().into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(Bitmap bm, GlideAnimation<? super Bitmap> glideAnimation) {
                                iv.setImageBitmap(bm);
                                if(onImageLoaded!=null){
                                    onImageLoaded.onImageLoaded(bm, iv);
                                }
                                if(!subscriber.isUnsubscribed()){
                                    subscriber.onNext(tag);
                                    subscriber.onCompleted();
                                }
                            }
                        });

                if(onTargetReadyCallBack!=null){
                    onTargetReadyCallBack.onTargetReady(target);
                }

                if(onSizeReadyCallBack!=null){
                    target.getSize(new SizeReadyCallback() {
                        @Override
                        public void onSizeReady(int width, int height) {
                                onSizeReadyCallBack.onSizeReady(width, height);
                        }
                    });
                }
            }
        });
    }



}
