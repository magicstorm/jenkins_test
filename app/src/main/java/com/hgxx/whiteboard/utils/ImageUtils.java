package com.hgxx.whiteboard.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;

import java.io.InputStream;

import rx.Observable;
import rx.Subscriber;

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



    public static <T> Observable<T> getLoadImageObserve(final Context context, final String url, final ImageView iv,
                                                                            final OnTargetReadyCallBack onTargetReadyCallBack, final OnSizeReadyCallBack onSizeReadyCallBack, final T tag){
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                Target<GlideDrawable> target = Glide.with(context)
                        .load(url).listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                e.printStackTrace();
                                if(!subscriber.isUnsubscribed()){
                                    subscriber.onError(e);
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                if(!subscriber.isUnsubscribed()){
                                    subscriber.onNext(tag);
                                    subscriber.onCompleted();
                                }
                                return false;
                            }
                        }).fitCenter().into(iv);

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
