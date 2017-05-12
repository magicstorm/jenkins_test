package com.hgxx.whiteboard.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.hgxx.whiteboard.WhiteBoardApplication;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.WebClient;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.utils.ImageUtils;
import com.hgxx.whiteboard.utils.ToastSingle;
import com.hgxx.whiteboard.views.HgScrollView;
import com.hgxx.whiteboard.views.drawview.DrawControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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


    private ArrayList<Target<Bitmap>> bmTargets = new ArrayList<>();
    private Integer connectionId;
    ScrollStat scrollStat;
    String presentationName;
    private ViewGroup presentationFrame;
    WeakReference<SocketClient> socketClientWeakReference;
    int presentationCount = 50;
    int totalHeight = 0;
    int totalWidth = 0;
    private String imageExt = ".png";

    public Presentation(){
        this(null);
    }

    public Presentation(String presentationName){
        this.presentationName = presentationName;
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




    public interface OnLoadPresentationCallBack{
        void onLoadPresentationCompleted();
        void onError(Throwable e);
        void onNext(Integer integer);
    }


    public void loadPresentation(Context context, final OnLoadPresentationCallBack onLoadPresentationCallBack){
//        int displayWidth = presentationFrame.getWidth();

        int displayWidth = getTotalWidth();
        presentationFrame.removeAllViews();
        totalHeight = 0;

        getLoadImagesObservable(context, displayWidth).subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                if(onLoadPresentationCallBack!=null){
                    onLoadPresentationCallBack.onLoadPresentationCompleted();
                }
            }

            @Override
            public void onError(Throwable e) {
                if(onLoadPresentationCallBack!=null){
                    onLoadPresentationCallBack.onError(e);
                }
            }

            @Override
            public void onNext(Integer integer) {
                if(onLoadPresentationCallBack!=null){
                    onLoadPresentationCallBack.onNext(integer);
                }
            }
        });

    }


    public Observable<Integer> getLoadImagesObservable(final Context context, final int displayWidth){
//        return Observable.create(new Observable.OnSubscribe<Integer>() {
//            @Override
//            public void call(final Subscriber<? super Integer> subscriber) {

        ArrayList<Observable<Integer>> obArray = new ArrayList<>();

                for(int i=0;i<getPresentationCount();i++){
                    final int index = i;
                    ImageView imageView = new ImageView(context);
                    LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(displayWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                    imageView.setLayoutParams(ivParams);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    imageView.setAdjustViewBounds(true);

                    Observable<Integer> imageObservable = ImageUtils.getLoadImageObserve(context, getPresentationUrl(i), imageView, new ImageUtils.OnTargetReadyCallBack<Target<Bitmap>>() {
                                @Override
                                public void onTargetReady(Target<Bitmap> target) {
                                    bmTargets.add(target);
                                }
                            }, new ImageUtils.OnSizeReadyCallBack() {
                                @Override
                                public void onSizeReady(int width, int height) {
                                    totalHeight+=height;
                                }
                            }, i
                    );

                    obArray.add(imageObservable);
                    presentationFrame.addView(imageView);
                }
//            }
//        });
        return Observable.merge(obArray);
    }


    /**
     * server side
     */
    public void initDrawMessage(final DrawControl drawControl){
        drawControl.setDrawListener(new DrawControl.DrawListener() {
            @Override
            public void onDrawStart() {
                getSocketClient().sendEvent(SocketClient.EVENT_SIG, "start");
            }

            @Override
            public void onDrawMove(float x, float y) {
                MovePoint mp = new MovePoint(x, y);
                mp.setDrawType(drawControl.getDrawType());
                mp.setFrameHeight(scrollStat.getTotalHeight());
                mp.setFrameWidth(getTotalWidth());
                mp.setStrokeWidth(drawControl.getStrokeWidth());
                getSocketClient().sendEvent(SocketClient.EVENT_PATH, mp);
            }

            @Override
            public void onDrawEnd() {
                getSocketClient().sendEvent(SocketClient.EVENT_SIG, "end");
            }
        });
    }

    public void initScrollMessage(final HgScrollView scrollView){
        scrollStat.setTotalHeight(scrollView.getChildAt(0).getHeight());
        scrollView.setOnScrollListener(new HgScrollView.OnScrollListener() {
            @Override
            public void onScrollChanged(int top, int oldt) {

                scrollStat.setCurrentHeight(top);
                scrollStat.setPresentationName(getPresentationName());

                    Gson gson = new Gson();
                    getSocketClient().sendEvent(SocketClient.EVENT_PRESENTATION, gson.toJson(scrollStat));
//                ToastSingle.showCenterToast("top: " + top + "total: " + totalHeight, Toast.LENGTH_SHORT);
            }
        });
    }



    public void initServer() throws IOException {
        initServerListener();
        connect();
        sendInitialMessage();
    }

    public void sendInitialMessage() {
        Gson gson = new Gson();
        getSocketClient().sendEvent(SocketClient.EVENT_PRESENTATION_INIT, gson.toJson(this.scrollStat));
    }

    public void initPresentation(float displayWidth, float displayHeight) throws IOException {

        ScrollStat initScrollStat = new ScrollStat(getPresentationName(), 0, 0);
        initScrollStat.setDisplay(new Display(displayWidth, displayHeight));
        this.scrollStat = initScrollStat;

    }


    public void connect(){
        final SocketClient socketClient = getSocketClient();
        socketClient.connect();
    }

    public void initServerListener(){
        final SocketClient socketClient = getSocketClient();
        socketClient.setEventListener(SocketClient.EVENT_CONNECTION, new SocketClient.EventListener() {
            @Override
            public void onEvent(Object... args) {
                try {
                    connectionId = Integer.valueOf(((JSONObject)args[0]).getString("id"));
                    socketClient.sendEvent("sig", "server");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * client side
     */



    /**
     * helpers
     */

    public String getPresentationUrl(int i){
        String url = Web.protocol+"://"+Web.address+":"+ Web.port + "/" + getPresentationName() + "/api_"+String.valueOf(i+1)+ imageExt;
        return url;
    }

    private SocketClient getSocketClient() {
        SocketClient socketClient = socketClientWeakReference.get();
        return socketClient;
    }

    private void callGlideTargetsLifeCycleMethod(String methodName){
        try {
            Method method = Glide.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            for(int i=0;i<bmTargets.size();i++){
                method.invoke(bmTargets.get(i));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * getters & setters
     */
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

    public ViewGroup getPresentationFrame() {
        return presentationFrame;
    }

    public void setPresentationFrame(ViewGroup presentationFrame) {
        this.presentationFrame = presentationFrame;
    }



    /**
     * life cycle
     */

    public void onDestroy(final Context context){
        callGlideTargetsLifeCycleMethod("onDestroy");
        new Thread(){
            @Override
            public void run() {
                Glide.get(context).clearDiskCache();
            }
        }.start();

    }

    public void onStart(){
        callGlideTargetsLifeCycleMethod("onStart");
    }

    public void onStop(){
        callGlideTargetsLifeCycleMethod("onStop");
    }

}
