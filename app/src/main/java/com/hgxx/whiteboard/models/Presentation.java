package com.hgxx.whiteboard.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.ImageInfo;
import com.google.gson.Gson;
import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.constants.Sock;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.utils.ToastSingle;
import com.hgxx.whiteboard.views.PageLoadListener;
import com.hgxx.whiteboard.views.PresentationAdapter;
import com.hgxx.whiteboard.views.drawview.DrawControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.widget.RelativeLayout.BELOW;


/**
 * Created by ly on 04/05/2017.
 */

public class Presentation {


    private String imageUrl;
    private Integer connectionId;
    ScrollStat scrollStat;
    String presentationName;
    private ViewGroup presentationFrame;
    WeakReference<SocketClient> socketClientWeakReference;
    int presentationCount = 50;
    int totalHeight = 0;
    int totalWidth = 0;
    private String imageExt = ".png";
    private String presentationId;
    private String roomId;

    private float sizeRatio;
    private ArrayList<Integer> pagePositions = new ArrayList<>();
    private int currentPage = 1;
    private OnLoadPresentationCallBack onLoad;
    private ImagePipeline imagePipeline;

    public static final String PRESENTATION_TYPE_WHITEBOARD = "-1";
    private int actHeight;
    private int wbHeight;

//    public void reload(Context context, String imageUrl, int count, String roomId, String presentationId){
//        setPresentationCount(count);
//        setPresentationId(this.presentationId);
//        setRoomId(this.roomId);
//        loadPresentation(context, imageUrl, onLoad);
//    }


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


    ArrayList<String> presentationNames = new ArrayList<>();
    class PresentationListener implements SocketClient.EventListener {
        Context mContext;

        public PresentationListener(Context context, String name){
            presentationNames.clear();
            presentationNames.add(name);
            this.mContext = context;
        }
        @Override
        public void onEvent(Object... args) {
            Gson gson = new Gson();
            ScrollStat scrollStat = gson.fromJson((String)args[0], ScrollStat.class);
            scrollToPosition(scrollStat, mContext);
        }


    }

    public void scrollToPosition(ScrollStat scrollStat, Context context) {
        boolean isChanged = false;

        if(isScrollStatChanged(scrollStat)){
            scrollStat.computeLocalScrollStat(totalHeight);
            isChanged = true;
        }
        if(isDisplayChanged(scrollStat)){
            scrollStat.getDisplay().computeLocalDisplaySize(context);
            isChanged = true;
        }


        if(isChanged){
            setScrollStat(scrollStat);
        }
    }

    private boolean isScrollStatChanged(ScrollStat scrollStat) {
        return !presentationId.equals(scrollStat.getPresentationId())||
                ((getScrollStat()==null)||!getScrollStat().equals(scrollStat));
    }

    private boolean isDisplayChanged(ScrollStat scrollStat){
        return !presentationId.equals(scrollStat.getPresentationId())||
                ((getScrollStat()==null)||!getScrollStat().getDisplay().equals(scrollStat.getDisplay()));
    }


    public interface OnLoadPresentationCallBack{
        void onLoadPresentationCompleted();
        void onNext(Integer integer);
    }


    private int loadedCount = 0;

    public void loadPresentation(Context context, String imageUrl, final OnLoadPresentationCallBack onLoadPresentationCallBack, PresentationAdapter presentationAdapter){
//        onLoad = onLoadPresentationCallBack;
//        int displayWidth = presentationFrame.getWidth();

        presentationFrame.removeAllViews();
        pagePositions.clear();
        loadedCount = 0;
        totalHeight = 0;
        currentPage = 1;
        if(TextUtils.isEmpty(imageUrl)){
            int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            setTotalHeight(screenHeight);
            wbHeight = screenHeight * 3;
            presentationFrame.getLayoutParams().height = wbHeight;
            actHeight = screenHeight;
            setTotalHeight(wbHeight);
            setTotalWidth(screenWidth);

            onLoadPresentationCallBack.onNext(0);
            onLoadPresentationCallBack.onLoadPresentationCompleted();

            return;
        }
        else{
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            presentationFrame.setLayoutParams(params);
        }
        this.imageUrl = imageUrl;

        final int displayWidth = getTotalWidth();

//        if(imagePipeline!=null){
//            imagePipeline.clearCaches();
//        }



        for(int j=0;j<getPresentationCount();j++){
            pagePositions.add(0);
        }

        for(int i=0;i<getPresentationCount();i++){
            final int index = i;
            actHeight = (int)(getTotalWidth()/sizeRatio+0.5f);
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(displayWidth, actHeight);

            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setLayoutParams(frameParams);

//            simpleDraweeView.setAdjustViewBounds(true);
            presentationFrame.addView(frameLayout);
            refreshPresentationHeight(actHeight, index);

            setLoadedCount(loadedCount + 1);
            if (onLoadPresentationCallBack != null) {
                onLoadPresentationCallBack.onNext(index);
            }
            if (onLoadPresentationCallBack != null && getLoadedCount() == getPresentationCount()) {
//                        int totalHeight = getTotalHeight();
                onLoadPresentationCallBack.onLoadPresentationCompleted();
            }


            if(i<3){
                loadImageView(context, index, presentationAdapter);
            }


//            ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
//                @Override
//                public void onFinalImageSet(
//                        String id,
//                        @Nullable ImageInfo imageInfo,
//                        @Nullable Animatable anim){
//                    if (imageInfo == null) {
//                        return;
//                    }
//
////                    simpleDraweeView.getLayoutParams().height = actHeight;
//
//
//
//
////                        presentationFrame.getLayoutParams().height = getTotalHeight();
////                        presentationFrame.measure(0,0);
////
////                        int w = presentationFrame.getMeasuredWidth();
////                        int h = presentationFrame.getMeasuredHeight();
////                        System.out.println("fuck");
//                    }
////                    QualityInfo qualityInfo = imageInfo.getQualityInfo();
////
////                            qualityInfo.getQuality(),
////                            qualityInfo.isOfGoodEnoughQuality(),
////                            qualityInfo.isOfFullQuality();
//
//                }
//
//                @Override
//                public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
////                    FLog.d("Intermediate image received");
//                }
//
//                @Override
//                public void onFailure(String id, Throwable throwable) {
////                    FLog.e(getClass(), throwable, "Error loading %s", id)
//                }
//            };

        }


    }


    private ImageView convertView;

    public void removeImageView(int index){
        FrameLayout frame = ((FrameLayout)presentationFrame.getChildAt(index));
        if(frame==null)return;
        ImageView newConvert = (ImageView)frame.getChildAt(0);
        setConvertView(newConvert);
        if(newConvert!=null){
            releaseImageViewResouce(newConvert);
            frame.removeView(newConvert);
        }
//        newConvert.setImageBitmap(null);
//        System.gc();
    }

    public synchronized void setConvertView(ImageView convertView) {
        this.convertView = convertView;
    }

    public synchronized void releaseImageViewResouce(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        imageView.setImageBitmap(null);
    }

    public synchronized void removeImages(int curPage) {

        int curPageIndex = getCurrentPage()-1;
        int pageDelta = curPage-curPageIndex;

        if(pageDelta>0){
            int searchStart = curPage-2;
            if(searchStart>=0){
                int searchBound = (curPageIndex-1)>=0?(curPageIndex-1):0;
                for(int i=searchStart;i>=searchBound;i--){
                    System.out.println("currentPage: "+curPage+"|remove: "+i);
                    removeImageView(i);
                }
            }
        }
        else{
            int seachStart = curPage+2;
            if(seachStart<=(getPresentationCount()-1)){
                int searchBound = (curPageIndex+1)<=(getPresentationCount()-1)?(curPageIndex+1):(getPresentationCount()-1);
                for(int i=seachStart;i<=searchBound;i++){
                    System.out.println("currentPage: "+curPage+"|remove: "+i);
                    removeImageView(i);
                }
            }
        }


    }


    public synchronized void loadNearByImages(Context context, int curPage, PresentationAdapter presentationAdapter){
        //TODO load use local diskCache
        System.out.println("load:" + (curPage-1));
        System.out.println("load:" + curPage);
        System.out.println("load:" + (curPage+1));
        loadImageView(context, curPage-1, presentationAdapter);
        loadImageView(context, curPage, presentationAdapter);
        loadImageView(context, curPage+1, presentationAdapter);
    }

    public void loadImageView(Context context, int index, PresentationAdapter presentationAdapter){
        FrameLayout frame = (FrameLayout)presentationFrame.getChildAt(index);
        if(frame==null||frame.getChildCount()>0)return;
        final ImageView  imageView;
        if(convertView!=null){
            imageView = convertView;
            convertView=null;
            System.out.println("load convertView: " + index);
        }
        else{
            imageView = new ImageView(context);
            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(getTotalWidth(), actHeight);
            imageView.setLayoutParams(ivParams);
            System.out.println("load new view");
        }
        if(imageView.getTag()!=null){
            System.out.println("妈比就你崩:" + imageView.getTag());
        }
        frame.addView(imageView);

        imageView.setTag(String.valueOf(index));

        presentationAdapter.loadImage(imageUrl+String.valueOf(index)+imageExt, imageView, String.valueOf(index), new PageLoadListener() {
            @Override
            public void onLoaded(){
            }

            @Override
            public void onFail() {
            }
        });
    }

    private synchronized int getLoadedCount(){
        return loadedCount;
    }

    private synchronized void refreshPresentationHeight(int height, int position){
        int nextPos = position+1;
        for(int i=nextPos;i<pagePositions.size();i++){
            pagePositions.set(i, pagePositions.get(i)+height);
        }
        setTotalHeight(getTotalHeight()+height);
    }


//    private int calculateLayoutedImageWidth(ImageView iv){
//        iv.measure(0, 0);
//        return (int)(iv.getMeasuredHeight() * totalWidth/(float)iv.getMeasuredWidth());
//    }

    /**
     * client side codes
     */
    public void listenPresentationChange(Context context){
        SocketClient socketClient = socketClientWeakReference.get();
        if(socketClient!=null&&socketClient.isConnected()){
            socketClient.setEventListener(SocketClient.EVENT_PRESENTATION, new PresentationListener(context, getPresentationName()));
        }
    }

    public void initClient(EventObserver eventObserver){
        initClientListeners(eventObserver);
        connect();
        sendRequest();
    }

    public interface EventObserver{
        void onPresentationInit(ScrollStat scrollStat);
        void onReceiveSignal(String signal);
        void onMove(MovePoint movePoint);
        void onConnection(String id);
        void onEnd(String id);
        void onClose(String presentationId);
    }


    private Handler uiHandler;



    private void initClientListeners(final EventObserver onReceiveEvent){
        final SocketClient socketClient = getSocketClient();
        socketClient.setEventListener(SocketClient.EVENT_PRESENTATION_INIT, new SocketClient.EventListener() {
            @Override
            public void onEvent(Object... args) {
                if(args[0]==null)return;
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
                if(onReceiveEvent!=null){
                    onReceiveEvent.onConnection(str);
                }
            }
        });

        socketClient.setEventListener(SocketClient.EVENT_PRESENTATION_END, new SocketClient.EventListener() {
            @Override
            public void onEvent(Object... args) {
                final String str = (String)args[0];
                if(onReceiveEvent!=null){
                    onReceiveEvent.onEnd(str);
                }
            }
        });

        socketClient.setEventListener(SocketClient.EVENT_PRESENTATION_CLOSE, new SocketClient.EventListener() {
            @Override
            public void onEvent(Object... args) {
                final String str = (String)args[0];
                if(onReceiveEvent!=null){
                    onReceiveEvent.onClose(str);
                }
            }
        });
    }

    private void sendRequest(){
        getSocketClient().sendEvent(SocketClient.EVENT_PRESENTATION_REQUEST, getRoomId());
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
        initScrollStat.setRoomId(roomId);
        initScrollStat.setPresentationId(presentationId);
        initScrollStat.setDisplay(new Display(displayWidth, displayHeight));
        this.scrollStat = initScrollStat;

    }

    public void sendEnd(){
        getSocketClient().sendEvent(SocketClient.EVENT_PRESENTATION_END, roomId);
    }

    public void sendClose(){
        getSocketClient().sendEvent(SocketClient.EVENT_PRESENTATION_CLOSE, presentationId);
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

    private boolean isJsonFieldNotNull(JSONObject jsonObject, String key) throws JSONException {
        return jsonObject.has(key)&&!TextUtils.isEmpty(jsonObject.getString(key))&&!jsonObject.getString(key).equals("null");
    }

    public int computeCurrentPage(int top, int oldt){
        int delta = top-oldt;
        if(delta==0)return currentPage;

        int searchDir = delta>0?1:(-1);
        int searchInterval = 1;

        int currentPage = getCurrentPage();
//        ToastSingle.showCenterToast("curPage: " + currentPage, Toast.LENGTH_SHORT);

        while (currentPage>=1&&currentPage<=getPresentationCount()){
            int nextPage = currentPage + searchInterval*searchDir;
            if(nextPage<1||nextPage>getPresentationCount()){
                break;
            }

            int nextTop = getPagePositions().get(nextPage-1);

//            ToastSingle.showCenterToast("nexttop: " + nextTop, Toast.LENGTH_SHORT);
            if(searchDir*top>=searchDir*nextTop){
                currentPage=nextPage;
//                ToastSingle.showCenterToast("next", Toast.LENGTH_SHORT);
            } else{
//                ToastSingle.showCenterToast("current", Toast.LENGTH_SHORT);
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

        final ScrollStat sc = scrollStat;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(onScrollStatChangeListener!=null){
                    onScrollStatChangeListener.onScrollStatChange(sc);
                }
            }
        });
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
//        if(scrollStat!=null){
//            scrollStat.setTotalHeight(totalHeight);
//        }
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

    }

    public void onStart(){
    }

    public void onStop(){
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

    public String getPresentationId() {
        return presentationId;
    }

    public void setPresentationId(String presentationId) {
        this.presentationId = presentationId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public float getSizeRatio() {
        return sizeRatio;
    }

    public void setSizeRatio(float sizeRatio) {
        this.sizeRatio = sizeRatio;
    }

    public synchronized void setLoadedCount(int loadedCount) {
        this.loadedCount = loadedCount;
    }

    public int getActHeight() {
        return actHeight;
    }

    public void setActHeight(int actHeight) {
        this.actHeight = actHeight;
    }
}
