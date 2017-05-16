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
import com.hgxx.whiteboard.entities.Signal;
import com.hgxx.whiteboard.models.Presentation;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by ly on 27/04/2017.
 */

public class WhiteBoardRcvActivity extends AppCompatActivity {


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


//        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        callGlideTargetsLifeCycleMethod("onStart");
    }

    @Override
    protected void onStop() {
//        callGlideTargetsLifeCycleMethod("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        socketClient.disconnect();
        socketClient.close();
//        callGlideTargetsLifeCycleMethod("onDestroy");


//        new Thread(){
//            @Override
//            public void run() {
//                Glide.get(WhiteBoardApplication.getContext()).clearDiskCache();
//            }
//        }.start();
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
        presentation = new Presentation("Test");
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
            Target<GlideDrawable> target = Glide.with(WhiteBoardApplication.getContext()).load(url).fitCenter().into(imageView);
            bmTargets.add(target);


            imageView.setAdjustViewBounds(true);
            scrollLl.addView(imageView);
        }
    }



    private void initSocketClient() {
        presentation.initClient(new Presentation.EventObserver() {
            @Override
            public void onPresentationInit(ScrollStat scrollStat) {
                initViews(scrollStat.getDisplay());
            }

            @Override
            public void onReceiveSignal(String str) {
                if (str.contains("end")) {
                    drawView.drawEnd();
                }
                else if(str.contains("start")){
                    drawView.setMoving(true);
                }
                else if(str.contains("clear")){
                    drawView.clear();
                }
                else if(str.contains("undo")){
                    drawView.cancel(-1);
                }

            }

            @Override
            public void onMove(MovePoint movePoint) {
                drawView.setStrokeWidth(movePoint.getStrokeWidth());
                drawView.setPaintColor(movePoint.getPaintColor());
                drawView.setDrawType(movePoint.getDrawType());

                if (drawView.isMoving()) {
                    drawView.setMoving(false);
                    drawView.startDraw(movePoint.getX(), movePoint.getY());
                } else {
                    drawView.drawMove(movePoint.getX(), movePoint.getY());
                }
            }

            @Override
            public void onConnection(String id) {
            }
        });
    }



}
