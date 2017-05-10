package com.hgxx.whiteboard.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.WhiteBoardApplication;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.models.Presentation;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawView;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ly on 27/04/2017.
 */

public class WhiteBoardRcvActivity extends AppCompatActivity {

    private DrawView wrb;

    private boolean moveStart = true;
    private int connectionId;

    private Socket socket;
    private SocketClient socketClient;
    private DrawViewController drawView;
    private ScrollView scrollView;
    private LinearLayout scrollLl;
    private Presentation presentation;

    private ArrayList<Target<GlideDrawable>> bmTargets = new ArrayList<>();
    private DrawLayout drawLayout;

    public synchronized void setMoveStart(boolean moveStart) {
        this.moveStart = moveStart;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board_rcv);
        findViews();
        drawView = new DrawViewController(drawLayout);
        drawView.setDrawable(false);

        initDatas();
        initSocketClient();

        sendObj(SocketClient.EVENT_SIG, "client");
        sendObj(SocketClient.EVENT_PRESENTATION_REQUEST, "test");

//        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        callGlideTargetsLifeCycleMethod("onStart");
    }

    @Override
    protected void onStop() {
        callGlideTargetsLifeCycleMethod("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        socketClient.disconnect();
        socketClient.close();
        callGlideTargetsLifeCycleMethod("onDestroy");
        super.onDestroy();
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


    private void initDatas(){
        presentation = new Presentation("test");
    }


    private void initViews(Display display){

        adjustDisplayArea(display.computeLocalDisplaySize(WhiteBoardApplication.getContext()));

        drawView.setWidth((int)display.getDisplayWidth());
        drawView.setHeight((int)display.getDisplayHeight());
        drawView.clear();


        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        initImageViews(presentation.getPresentationCount(), drawView.getWidth());


        if(scrollView.getViewTreeObserver().isAlive()){
            scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                                   @Override
                                                                   public void onGlobalLayout() {
                    if(presentation!=null){
                        presentation.setTotalHeight(scrollView.getChildAt(0).getHeight());
                        presentation.setTotalWidth(scrollView.getChildAt(0).getWidth());
                        drawView.setWidth(presentation.getTotalWidth());
                        drawView.setHeight(presentation.getTotalHeight());
                    }
        }
    });
        }
        else{
            if(presentation!=null){
                presentation.setTotalHeight(scrollView.getChildAt(0).getHeight());
                presentation.setTotalWidth(scrollView.getChildAt(0).getWidth());
                drawView.setWidth(presentation.getTotalWidth());
                drawView.setHeight(presentation.getTotalHeight());
            }
        }


                presentation.setOnScrollStatChangeListener(new Presentation.OnScrollStatChange() {
//                    int i = 0;
                    @Override
                    public void onScrollStatChange(ScrollStat scrollStat) {
                        final int scrollTop = (int) scrollStat.getCurrentHeight();
                        scrollView.scrollTo(0, scrollTop);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawView.scrollTo(scrollTop);
                            }
                        });

                    }
                });


                presentation.listenPresentationChange(WhiteBoardApplication.getContext());

    }

    private void adjustDisplayArea(final Display display){
//        int sw = getResources().getDisplayMetrics().widthPixels;
//        int sh = getResources().getDisplayMetrics().heightPixels;
//        int ww = (int)display.getDisplayWidth();

        presentation.setTotalHeight((int)display.getDisplayHeight());
        scrollView.getLayoutParams().width = (int)display.getDisplayWidth();
        presentation.setTotalWidth((int)display.getDisplayWidth());
        scrollView.invalidate();
    }


    private void findViews(){
        scrollView = (ScrollView)findViewById(R.id.sv);
        scrollLl = (LinearLayout) findViewById(R.id.ll);
        drawLayout = (DrawLayout)findViewById(R.id.drawRcvView);
    }

    private void initImageViews(int count, int width){
        scrollLl.removeAllViews();
        for(int i=0;i<count;i++){
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(ivParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);


            String url = Web.protocol+"://"+Web.address+":"+ Web.port + "/Test"+ "/api_"+String.valueOf(i+1)+".png";
            Target<GlideDrawable> target = Glide.with(this).load(url).fitCenter().into(imageView);
            bmTargets.add(target);




            imageView.setAdjustViewBounds(true);
            scrollLl.addView(imageView);
        }
    }

    private synchronized void sendObj(String eventName, Object... datas){
        initSocketClient();
        socketClient.sendEvent(eventName, datas);
    }

    private void initSocketClient() {
        if(socketClient ==null){
            socketClient = SocketClient.getInstance();

            socketClient.setEventListener(SocketClient.EVENT_PRESENTATION_INIT, new SocketClient.EventListener() {
                @Override
                public void onEvent(Object... args) {
                    Gson gson = new Gson();
                    final ScrollStat scrollStat = gson.fromJson((args[0]).toString(), ScrollStat.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initViews(scrollStat.getDisplay());
                        }
                    });
                }
            });


            socketClient.setEventListener(SocketClient.EVENT_SIG, new SocketClient.EventListener() {

                @Override
                public void onEvent(Object... args) {
                    String str = (String)args[0];
                    if (str.contains("end")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                        System.out.println("moveend");
//                                        System.out.println("\n\n\n\n\n");
                                drawView.drawEnd();
                            }
                        });
                    }
                    else if(str.contains("start")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                        System.out.println("movestart");
//                                        System.out.println("\n\n\n\n\n");
                                drawView.setMoving(true);
                            }
                        });
                    }
                    else if(str.contains("clear")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawView.clear();
                            }
                        });
                    }
                    else if(str.contains("undo")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawView.cancel(-1);
                            }
                        });
                    }



                    System.out.println(args[0].toString());
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

                        float wi = w * drawView.getWidth() / fw;
                        float he = h * drawView.getHeight() / fh;

                        final MovePoint mp = new MovePoint(wi, he);

                        if(isJsonFieldNotNull(jsonObject, "strokeWidth")){

                            float rawWidth = Float.valueOf(jsonObject.getString("strokeWidth"));

                            float strokeWidth =  rawWidth * drawView.getWidth() / fw;
                            drawView.setStrokeWidth(strokeWidth);
                        }

                        if(isJsonFieldNotNull(jsonObject, "paintColor")){
                            String color = jsonObject.getString("paintColor");
                            drawView.setPaintColor(color);
                        }

                        if(isJsonFieldNotNull(jsonObject, "drawType")){
                            String drawType = jsonObject.getString("drawType");
                            drawView.setDrawType(drawType);
                        }

    //                            System.out.println("x: " + String.valueOf(mp.getX()) + "y: " + String.valueOf(mp.getY()) + "\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                int w = wrb.getCurWidth();
//                                int h = wrb.getCurHeight();
                                if (drawView.isMoving()) {
    //                                            System.out.println("movestart");
                                    drawView.setMoving(false);
                                    drawView.startDraw(mp.getX(), mp.getY());
                                } else {
    //                                            System.out.println("move");
                                    drawView.drawMove(mp.getX(), mp.getY());
                                }
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

                    try {
                        connectionId = Integer.valueOf(((JSONObject)args[0]).getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
        if(!socketClient.isConnected()){
            socketClient.connect();
        }
    }

    private boolean isJsonFieldNotNull(JSONObject jsonObject, String key) throws JSONException {
        return jsonObject.has(key)&&!TextUtils.isEmpty(jsonObject.getString(key))&&!jsonObject.getString(key).equals("null");
    }


}
