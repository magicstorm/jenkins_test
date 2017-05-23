package com.hgxx.whiteboard.models;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.google.gson.Gson;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.utils.ToastSingle;
import com.hgxx.whiteboard.views.drawview.DrawControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


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


    private int loadedCount = 0;

    public void loadPresentation(Context context, String imageUrl, final OnLoadPresentationCallBack onLoadPresentationCallBack){
//        int displayWidth = presentationFrame.getWidth();
        if(TextUtils.isEmpty(imageUrl))return;
        this.imageUrl = imageUrl;

        int displayWidth = getTotalWidth();
        presentationFrame.removeAllViews();
        totalHeight = 0;


        for(int j=0;j<getPresentationCount();j++){
            pagePositions.add(0);
        }

        for(int i=0;i<getPresentationCount();i++){
            final int index = i;
            SimpleDraweeView simpleDraweeView = new SimpleDraweeView(context);
            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(displayWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            simpleDraweeView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            simpleDraweeView.setLayoutParams(ivParams);
            simpleDraweeView.setAdjustViewBounds(true);

            presentationFrame.addView(simpleDraweeView);

            ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(
                        String id,
                        @Nullable ImageInfo imageInfo,
                        @Nullable Animatable anim){
                    if (imageInfo == null) {
                        return;
                    }

//                    imageInfo.getWidth();
                    int height = imageInfo.getHeight();
                    refreshPresentationHeight(height, index);

                    if(onLoadPresentationCallBack!=null){
                        onLoadPresentationCallBack.onNext(index);
                    }

                    if(onLoadPresentationCallBack!=null&&loadedCount==getPresentationCount()){
                        onLoadPresentationCallBack.onLoadPresentationCompleted();
                    }
//                    QualityInfo qualityInfo = imageInfo.getQualityInfo();
//
//                            qualityInfo.getQuality(),
//                            qualityInfo.isOfGoodEnoughQuality(),
//                            qualityInfo.isOfFullQuality();
                }

                @Override
                public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
//                    FLog.d("Intermediate image received");
                }

                @Override
                public void onFailure(String id, Throwable throwable) {
//                    FLog.e(getClass(), throwable, "Error loading %s", id)
                }
            };

            Uri uri = Uri.parse(imageUrl+(i+1) + imageExt);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setControllerListener(controllerListener)
                    .setUri(uri)
                    .build();


            simpleDraweeView.setController(controller);


        }

    }


    private synchronized void refreshPresentationHeight(int height, int position){
        int nextPos = position+1;
        for(int i=nextPos;i<pagePositions.size();i++){
            pagePositions.set(i, pagePositions.get(nextPos)+height);
        }
        setTotalHeight(getTotalHeight()+height);
        loadedCount+=1;
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
                onReceiveEvent.onConnection(str);
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
        ToastSingle.showCenterToast("curPage: " + currentPage, Toast.LENGTH_SHORT);

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
}
