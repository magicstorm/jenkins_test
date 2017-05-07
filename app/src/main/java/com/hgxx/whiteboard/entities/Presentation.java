package com.hgxx.whiteboard.entities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.WebClient;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    int presentationCount = 5;
    List<Bitmap> presentationBitmaps = Arrays.asList(new Bitmap[presentationCount]);
    int totalHeight = 0;

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

    public interface OnPresentationDownloadedComplete{
        void onPresentationDownloadComplete(int index);
    }

    public Observable<Integer> getPresentationImages(final OnPresentationDownloadedComplete onPresentationDownloadedComplete){
        WebClient.PresentationService presentationService = WebClient.getInstance().getPresentationService();

        Observable<Integer> totalOb = null;
        for(int i=0;i<presentationCount;i++){

            final ReplaySubject<Integer> sub = ReplaySubject.create();

            final int index = i;
            Observable<ResponseBody> presentationImageObservable = presentationService.getPresentationImage(presentationName, "api_"+String.valueOf(i+1)+".png");
            presentationImageObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(new Observer<ResponseBody>() {
                @Override
                public void onCompleted() {
                    sub.onCompleted();
                }

                @Override
                public void onError(Throwable e) {
                    sub.onError(e);
                }

                @Override
                public void onNext(ResponseBody response) {
                    InputStream inputStream = response.byteStream();
                    presentationBitmaps.set(index, BitmapFactory.decodeStream(inputStream));
                    new MainHandler().post(new PresentationDownloadedHandler(onPresentationDownloadedComplete, index));
                    sub.onNext(index);
                }
            });

            if(totalOb==null){
                totalOb = sub.asObservable();
            }
            else{
                totalOb = Observable.merge(totalOb, sub);
            }
        }
        return totalOb;
    }

    public int computePresentationHeight(Context context){
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        float height = 0;
        for(int i=0;i<presentationCount;i++){
            Bitmap bm = presentationBitmaps.get(i);
            height += bm.getHeight()*screenWidth/(float)bm.getWidth();
        }
        this.totalHeight = (int)height;
        return totalHeight;
    }


    private static class MainHandler extends Handler{
        public MainHandler(){
            super(Looper.getMainLooper());
        }
    }

    private static class PresentationDownloadedHandler implements Runnable {
        OnPresentationDownloadedComplete onPresentationDownloadedComplete;
        int index = 0;
        public PresentationDownloadedHandler(OnPresentationDownloadedComplete onPresentationDownloadedComplete, int index){
            this.onPresentationDownloadedComplete = onPresentationDownloadedComplete;
            this.index = index;
        }
        @Override
        public void run(){
            if(onPresentationDownloadedComplete!=null){
                onPresentationDownloadedComplete.onPresentationDownloadComplete(index);
            }
        }

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

    public List<Bitmap> getPresentationBitmaps() {
        return presentationBitmaps;
    }

    public void setPresentationBitmaps(List<Bitmap> presentationBitmaps) {
        this.presentationBitmaps = presentationBitmaps;
    }

    public int getTotalHeight() {
        return totalHeight;
    }

    public void setTotalHeight(int totalHeight) {
        this.totalHeight = totalHeight;
    }
}
