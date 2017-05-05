package com.hgxx.whiteboard.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ScrollView;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.network.constants.Sock;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.views.drawview.DrawView;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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


    public synchronized void setMoveStart(boolean moveStart) {
        this.moveStart = moveStart;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board_rcv);
        findViews();
        drawView = new DrawViewController(wrb);

        //test code
//        new Thread(){
//            @Override
//            public void run() {
//                wrb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        wrb.touch_start(0, 0);
//                        wrb.touch_move(500, 500);
//                        wrb.touch_up();
//
//                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(WhiteBoardRcvActivity.this, "清屏", Toast.LENGTH_SHORT);
//                                wrb.clear();
//                            }
//                        }, 3000);
//                    }
//                });
//            }
//        }.start();

        sendObj(SocketClient.EVENT_SIG, "client");
    }

    private void findViews(){
        wrb = (DrawView)findViewById(R.id.drawRcvView);
        scrollView = (ScrollView)findViewById(R.id.sv);
    }




    private synchronized void sendObj(String eventName, Object... datas){
        if(socketClient ==null){
            socketClient = SocketClient.getInstance();
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

                        float wi = w * getResources().getDisplayMetrics().widthPixels / fw;
                        float he = h * getResources().getDisplayMetrics().heightPixels / fh;

                        final MovePoint mp = new MovePoint(wi, he);
                        if(jsonObject.has("strokWidth")&&!TextUtils.isEmpty(jsonObject.getString("stokeWidth"))&&!jsonObject.getString("strokeWidth").equals("null")){

                            float rawWidth = Float.valueOf(jsonObject.getString("strokeWidth"));

                            float strokeWidth =  rawWidth * getResources().getDisplayMetrics().widthPixels / fw;
                            drawView.setStrokeWidth(strokeWidth);
                        };

                        if(jsonObject.has("color")){
                            String color = jsonObject.getString("color");

                            drawView.setPaintColor(color);
                        }


    //                            System.out.println("x: " + String.valueOf(mp.getX()) + "y: " + String.valueOf(mp.getY()) + "\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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

        socketClient.sendEvent(eventName, datas);
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
