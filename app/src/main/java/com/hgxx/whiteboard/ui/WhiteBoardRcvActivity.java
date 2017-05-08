package com.hgxx.whiteboard.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.WhiteBoardApplication;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.entities.Presentation;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.network.constants.Sock;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.utils.ViewHelpers;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawScrollView;
import com.hgxx.whiteboard.views.drawview.DrawView;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import rx.Observable;
import rx.Observer;

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

    private HashMap<String, ArrayList<ImageView>> presentations;
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

    private void initDatas(){
        presentation = new Presentation("test");
        presentations = new HashMap<>();
    }

    public static class ImageDownloaded implements Presentation.OnPresentationDownloadedComplete{
        HashMap<String, ArrayList<ImageView>> presentations;
        Presentation presentation;

        public ImageDownloaded(HashMap<String, ArrayList<ImageView>> presentations, Presentation presentation){
            this.presentations = presentations;
            this.presentation = presentation;
        }

        @Override
        public void onPresentationDownloadComplete(int index) {
            ArrayList<ImageView> imageViews = presentations.get(presentation.getPresentationName());
            if(imageViews.size()>index){
                imageViews.get(index).setImageBitmap(presentation.getPresentationBitmaps().get(index));
            }
        }
    }

    private void initViews(Display display){

        adjustDisplayArea(display.computeLocalDisplaySize(WhiteBoardApplication.getContext()));

        drawView.setWidth((int)display.getDisplayWidth());
        drawView.setHeight((int)display.getDisplayHeight());


        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        presentations.put(presentation.getPresentationName(), new ArrayList<ImageView>());
        initImageViews(presentation.getPresentationCount());
        Observable<Integer> ob = presentation.getPresentationImages(new ImageDownloaded(presentations, presentation));
        ob.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
//                presentation.computePresentationHeight(WhiteBoardApplication.getContext());

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
//                        System.out.println(
//                                "滚动变化" + String.valueOf(i) + ": " +
//                                        "currentHeight=" + scrollStat.getCurrentHeight() +
//                                        "|totalHeight=" + scrollStat.getTotalHeight()
//                        );
                        final int scrollTop = (int) scrollStat.getCurrentHeight();
                        scrollView.scrollTo(0, scrollTop);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawView.scrollTo(scrollTop);
                            }
                        });

//                        adjustDisplayArea(scrollStat.getDisplay());
//                        i++;
                    }
                });


                presentation.listenPresentationChange(WhiteBoardApplication.getContext());

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {

            }
        });
    }

    private void adjustDisplayArea(final Display display){
        int sw = getResources().getDisplayMetrics().widthPixels;
        int sh = getResources().getDisplayMetrics().heightPixels;
//                for(int i=0;i<scrollView.getChildCount();i++){
//                    scrollView.getChildAt(i).getLayoutParams().width = (int)display.getDisplayWidth();
//                    scrollView.getChildAt(i).invalidate();
//                }
        int ww = (int)display.getDisplayWidth();

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

    private void initImageViews(int count){
        for(int i=0;i<count;i++){
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(ivParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            imageView.setAdjustViewBounds(true);
            scrollLl.addView(imageView);
            presentations.get(presentation.getPresentationName()).add(imageView);
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


                    System.out.println(args[0].toString());
                }
            });

//            socketClient.setEventListener(SocketClient.EVENT_PRESENTATION, new SocketClient.EventListener() {
//                @Override
//                public void onEvent(Object... args) {
//                }
//            });

//
//            socketClient.setEventListener(SocketClient.EVENT_PRESENTATION_INIT, new SocketClient.EventListener() {
//                @Override
//                public void onEvent(Object... args) {
//
//                }
//            });

            socketClient.setEventListener(SocketClient.EVENT_PATH, new SocketClient.EventListener() {
                @Override
                public void onEvent(Object... args) {
                    try {
//                        String str = args[0].toString();
//                        JSONObject jsonObject = null;
    //                            System.out.println("\nstring is: " + str);
                        JSONObject jsonObject = new JSONObject((String)args[0]);
//                        jsonObject = new JSONObject(str);
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


    public synchronized void setSocketIfNoSocket() throws IOException {
        if(socket ==null){
            socket = new Socket(Sock.serverIP, Sock.serverPort);
        }
    }

    private synchronized void sendString(final String str) {
        new Thread(){
            @Override
            public void run() {
                try {
                    setSocketIfNoSocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(TextUtils.isEmpty(str))return;
                OutputStream os=null;
                try {
                    if(socket !=null){
                        os = socket.getOutputStream();
                        os.write(str.getBytes());
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @Override
    protected void onDestroy() {
        socketClient.disconnect();
        socketClient.close();

        super.onDestroy();
    }
}
