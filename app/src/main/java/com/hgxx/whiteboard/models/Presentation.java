package com.hgxx.whiteboard.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.entities.Signal;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.utils.ImageUtils;
import com.hgxx.whiteboard.views.drawview.DrawControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import rx.Observable;
import rx.Observer;

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

    private ArrayList<Integer> pagePositions = new ArrayList<>();
    private int currentPage = 1;


    public Presentation(){
        this(null);
    }

    public Presentation(String presentationName){
        this.presentationName = presentationName;
        setSocketClient(SocketClient.getInstance());
        uiHandler = new Handler(Looper.getMainLooper());
    }


    public interface OnScrollStatChange{
        void onScrollStatChange(ScrollStat scrollStat);
    }

    private OnScrollStatChange onScrollStatChangeListener;

    public void setOnScrollStatChangeListener(OnScrollStatChange onScrollStatChangeListener) {
        this.onScrollStatChangeListener = onScrollStatChangeListener;
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
                    presentationFrame.addView(imageView);
                    Observable<Integer> imageObservable = ImageUtils.getLoadImageObserve(context, getPresentationUrl(i), imageView, new ImageUtils.OnTargetReadyCallBack<Target<Bitmap>>() {
                                @Override
                                public void onTargetReady(Target<Bitmap> target) {
                                    bmTargets.add(target);
                                }
                            }, new ImageUtils.OnImageLoaded<Bitmap, ImageView>() {
                                @Override
                                public void onImageLoaded(Bitmap bm, ImageView iv) {
                                    pagePositions.add(getTotalHeight());
                                    setTotalHeight(getTotalHeight()+calculateLayoutedImageWidth(iv));

                                }
                            }, i + 1
                    );

                    obArray.add(imageObservable);
                }
//            }
//        });
        return Observable.merge(obArray);
    }

    private int calculateLayoutedImageWidth(ImageView iv){
        iv.measure(0, 0);
        return (int)(iv.getMeasuredHeight() * totalWidth/(float)iv.getMeasuredWidth());
    }

    /**
     * client side codes
     */
    public void initClient(EventObserver eventObserver){
        initClientListeners(eventObserver);
        connect();
    }

    public interface EventObserver{
        void onPresentationInit(ScrollStat scrollStat);
        void onReceiveSignal(String signal);
        void onMove(MovePoint movePoint);
        void onConnection(String id);
    }


    private Handler uiHandler;

    private void initClientListeners(final EventObserver onReceiveEvent){
        final SocketClient socketClient = getSocketClient();
        socketClient.setEventListener(SocketClient.EVENT_PRESENTATION_INIT, new SocketClient.EventListener() {
            @Override
            public void onEvent(Object... args) {
                Gson gson = new Gson();
                final ScrollStat scrollStat = gson.fromJson((args[0]).toString(), ScrollStat.class);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onReceiveEvent.onPresentationInit(scrollStat);
                    }
                });

            }
        });


        socketClient.setEventListener(SocketClient.EVENT_SIG, new SocketClient.EventListener() {

            @Override
            public void onEvent(Object... args) {
                final String str = (String)args[0];
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onReceiveEvent.onReceiveSignal(str);
                    }
                });

//                System.out.println(args[0].toString());
            }
        });


        socketClient.setEventListener(SocketClient.EVENT_PATH, new SocketClient.EventListener() {
            @Override
            public void onEvent(Object... args) {
                try {

                    JSONObject jsonObject = new JSONObject((String)args[0]);
                    if (jsonObject == null) {
                        return;
                    }

                    float w = Float.valueOf(jsonObject.getString("x"));
                    float h = Float.valueOf(jsonObject.getString("y"));
                    float fw = Float.valueOf(jsonObject.getString("frameWidth"));
                    float fh = Float.valueOf(jsonObject.getString("frameHeight"));

//                        int hh = drawView.getHeight();
                    float wi = w * getTotalWidth() / fw;
                    float he = h * getTotalHeight() / fh;

                    final MovePoint mp = new MovePoint(wi, he);

                    if(isJsonFieldNotNull(jsonObject, "strokeWidth")){
                        float rawWidth = Float.valueOf(jsonObject.getString("strokeWidth"));
                        float strokeWidth =  rawWidth * getTotalWidth() / fw;
                        mp.setStrokeWidth(strokeWidth);
//                            System.out.println("totalHeight: " + fw + "|strokeWidth: " + rawWidth);
                    }

                    if(isJsonFieldNotNull(jsonObject, "paintColor")){
                        String color = jsonObject.getString("paintColor");
                        mp.setPaintColor(color);
                    }

                    if(isJsonFieldNotNull(jsonObject, "drawType")){
                        String drawType = jsonObject.getString("drawType");
                        mp.setDrawType(drawType);
                    }

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onReceiveEvent.onMove(mp);
                        }
                    });

                } catch (JSONException e){
                    e.printStackTrace();
                }

            }
        });

        socketClient.setEventListener(SocketClient.EVENT_CONNECTION, new SocketClient.EventListener() {
            @Override
            public void onEvent(Object... args) {
                String str = null;
                try {
                    str = ((JSONObject)args[0]).getString("id");
                    connectionId = Integer.valueOf(str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socketClient.sendEvent(SocketClient.EVENT_SIG, "client");
                socketClient.sendEvent(SocketClient.EVENT_PRESENTATION_REQUEST, "Test");
                onReceiveEvent.onConnection(str);
            }
        });

    }




    /**
     * server side codes
     */

    public void clearPaint(){
        getSocketClient().sendEvent("sig", "clear");
    }

    public void undoPaint(){
        getSocketClient().sendEvent("sig", "undo");
    }

    public void sendScroll(float top){
        scrollStat.setCurrentHeight(top);
        scrollStat.setPresentationName(getPresentationName());
        Gson gson = new Gson();
        getSocketClient().sendEvent(SocketClient.EVENT_PRESENTATION, gson.toJson(scrollStat));
    }

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
                mp.setDrawType(drawControl.getDrawType());
                mp.setPaintColor(drawControl.getPaintColor());
                getSocketClient().sendEvent(SocketClient.EVENT_PATH, mp);
            }

            @Override
            public void onDrawEnd() {
                getSocketClient().sendEvent(SocketClient.EVENT_SIG, "end");
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
    public void listenPresentationChange(Context context){
        SocketClient socketClient = socketClientWeakReference.get();
        if(socketClient!=null&&socketClient.isConnected()){
            socketClient.setEventListener(SocketClient.EVENT_PRESENTATION, new PresentationListener(context, getPresentationName()));
        }
    }



    /**
     * helpers
     */

    private boolean isJsonFieldNotNull(JSONObject jsonObject, String key) throws JSONException {
        return jsonObject.has(key)&&!TextUtils.isEmpty(jsonObject.getString(key))&&!jsonObject.getString(key).equals("null");
    }

    public int computeCurrentPage(int top, int oldt){
        int delta = top-oldt;
        if(delta==0)return currentPage;

        int searchDir = delta>0?1:(-1);
        int searchInterval = 1;

        int currentPage = getCurrentPage();

        while (currentPage>=1&&currentPage<=getPresentationCount()){
            int nextPage = currentPage + searchInterval*searchDir;
            if(nextPage<1||nextPage>getPresentationCount()){
                break;
            }

            int nextTop = getPagePositions().get(nextPage-1);

            if(searchDir*top>=searchDir*nextTop){
                currentPage=nextPage;
            } else{
                return currentPage;
            }
        }
        return currentPage;
    }


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
        if(scrollStat!=null){
            scrollStat.setTotalHeight(totalHeight);
        }
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

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public ArrayList<Integer> getPagePositions() {
        return pagePositions;
    }

    public void setPagePositions(ArrayList<Integer> pagePositions) {
        this.pagePositions = pagePositions;
    }
}
