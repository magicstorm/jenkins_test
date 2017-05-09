package com.hgxx.whiteboard.entities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hgxx.whiteboard.WhiteBoardApplication;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.WebClient;
import com.hgxx.whiteboard.utils.ImageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * Created by ly on 04/05/2017.
 */

public class Presentation {

    ScrollStat scrollStat;
    String presentationName;
    String url;
    WeakReference<SocketClient> socketClientWeakReference;
    int presentationCount = 50;
    int totalHeight = 0;
    int totalWidth = 0;

    public Presentation(){
        this(null);
    }

    public Presentation(String presentationName){
        this(presentationName, null);
    }

    public Presentation(String presentationName, String url){
        this.presentationName = presentationName;
        this.url = url;
                setSocketClient(SocketClient.getInstance());
    }

    public interface OnScrollStatChange{
        void onScrollStatChange(ScrollStat scrollStat);
    }

    private OnScrollStatChange onScrollStatChangeListener;

    public void setOnScrollStatChangeListener(OnScrollStatChange onScrollStatChangeListener) {
        this.onScrollStatChangeListener = onScrollStatChangeListener;
    }






    public void listenPresentationChange(Context context){
        SocketClient socketClient = socketClientWeakReference.get();
        if(socketClient!=null&&socketClient.isConnected()){
            socketClient.setEventListener(SocketClient.EVENT_PRESENTATION, new PresentationListener(context, getPresentationName()));
        }
    }

    class PresentationListener implements SocketClient.EventListener {
        ArrayList<String> presentationNames = new ArrayList<>();
        Context mContext;

        public PresentationListener(Context context, String name){
            presentationNames.add(name);
            this.mContext = context;
        }
        @Override
        public void onEvent(Object... args) {
            Gson gson = new Gson();
            ScrollStat scrollStat = gson.fromJson((String)args[0], ScrollStat.class);
            boolean isChanged = false;

            if(isScrollStatChanged(scrollStat)){
                scrollStat.computeLocalScrollStat(totalHeight);
                isChanged = true;
            }
            if(isDisplayChanged(scrollStat)){
                scrollStat.getDisplay().computeLocalDisplaySize(mContext);
                isChanged = true;
            }



            if(isChanged){
                setScrollStat(scrollStat);
            }
        }

        private boolean isScrollStatChanged(ScrollStat scrollStat) {
            return presentationNames.contains(scrollStat.getPresentationName().trim())&&
                    ((getScrollStat()==null)||!getScrollStat().equals(scrollStat));
        }

        private boolean isDisplayChanged(ScrollStat scrollStat){
            return presentationNames.contains(scrollStat.getPresentationName().trim())&&
                    ((getScrollStat()==null)||!getScrollStat().getDisplay().equals(scrollStat.getDisplay()));
        }
    }




    public void setSocketClient(SocketClient socketClient) {
        this.socketClientWeakReference = new WeakReference<>(socketClient);
    }

    public ScrollStat getScrollStat() {
        return scrollStat;
    }

    public void setScrollStat(ScrollStat scrollStat) {
        this.scrollStat = scrollStat;
        if(onScrollStatChangeListener!=null){
            onScrollStatChangeListener.onScrollStatChange(this.scrollStat);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getPresentationName() {
        return presentationName;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public int getPresentationCount() {
        return presentationCount;
    }

    public void setPresentationCount(int presentationCount) {
        this.presentationCount = presentationCount;
    }


    public int getTotalHeight() {
        return totalHeight;
    }

    public void setTotalHeight(int totalHeight) {
        this.totalHeight = totalHeight;
    }

    public int getTotalWidth() {
        return totalWidth;
    }

    public void setTotalWidth(int totalWidth) {
        this.totalWidth = totalWidth;
    }
}
