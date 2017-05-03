package com.hgxx.whiteboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.hgxx.whiteboard.constants.Sock;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.views.drawview.DrawRcvView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by ly on 27/04/2017.
 */

public class WhiteBoardRcvActivity extends AppCompatActivity {

    private DrawRcvView wrb;
    private ServerSocket serverSocket;

    private boolean moveStart = true;
    private Socket s;
    private InputStream is;
    private int connectionId;

    private Stack<String> readCache = new Stack<String>();
    private ArrayList<String> partialBuff = new ArrayList<>();
    private BufferedReader br;
    private Socket socket;
    private SocketClient socketClient;

    public synchronized boolean isMoveStart() {
        return moveStart;
    }

    public synchronized void setMoveStart(boolean moveStart) {
        this.moveStart = moveStart;
    }

    private synchronized void initSocketServer(){

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wrb = new DrawRcvView(this);
        setContentView(wrb);


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




//        sendString("client\n");
        sendObj(SocketClient.EVENT_SIG, "client");

        //socket code
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                    while (socket!=null&&socket.isConnected()) {
//                        String str = br.readLine();
//                        if (!TextUtils.isEmpty(str)) {
////                            System.out.println("\nreceived point: ");
//
//                            if (str.contains("end")) {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
////                                        System.out.println("moveend");
////                                        System.out.println("\n\n\n\n\n");
//                                        wrb.touch_up();
//                                    }
//                                });
//                                continue;
//                            }
//                            else if(str.contains("start")){
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
////                                        System.out.println("movestart");
////                                        System.out.println("\n\n\n\n\n");
//                                        setMoveStart(true);
//                                    }
//                                });
//                                continue;
//                            }
//                            else if(str.contains("clear")){
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        wrb.clear();
//                                    }
//                                });
//                            }
//
//
////                            ArrayList<String> movePoints = readPoints(str);
//
//                            JSONObject jsonObject = null;
////                            System.out.println("\nstring is: " + str);
//                            try {
//                                jsonObject = new JSONObject(str);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            if (jsonObject == null) {
//                                continue;
//                            }
//
//                            float w = Float.valueOf(jsonObject.getString("x"));
//                            float h = Float.valueOf(jsonObject.getString("y"));
//                            float fw = Float.valueOf(jsonObject.getString("frameWidth"));
//                            float fh = Float.valueOf(jsonObject.getString("frameHeight"));
//
//                            float wi = w * getResources().getDisplayMetrics().widthPixels / fw;
//                            float he = h * getResources().getDisplayMetrics().heightPixels / fh;
//
//                            final MovePoint mp = new MovePoint(wi, he);
//                            if(jsonObject.has("strokWidth")&&!TextUtils.isEmpty(jsonObject.getString("stokeWidth"))&&!jsonObject.getString("strokeWidth").equals("null")){
//
//                                float rawWidth = Float.valueOf(jsonObject.getString("strokeWidth"));
//
//                                float strokeWidth =  rawWidth * getResources().getDisplayMetrics().widthPixels / fw;
//                                wrb.setStrokeWidth(strokeWidth);
//                            };
//
//                            if(jsonObject.has("color")){
//                                String color = jsonObject.getString("color");
//
//                                wrb.setPaintColor(color);
//                            }
//
//
////                            System.out.println("x: " + String.valueOf(mp.getX()) + "y: " + String.valueOf(mp.getY()) + "\n");
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (isMoveStart()) {
////                                            System.out.println("movestart");
//                                        setMoveStart(false);
//                                        wrb.touch_start(mp.getX(), mp.getY());
//                                    } else {
////                                            System.out.println("move");
//                                        wrb.touch_move(mp.getX(), mp.getY());
//                                    }
//                                }
//                            });
//
//                        }
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } finally {
//                        try {
//                            if(socket!=null){
//                                socket.close();
//                            }
//                            if(is!=null){
//                                is.close();
//                            }
//                            if(s!=null){
//                                s.close();
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                }
//
//            }
//        }.start();



//server code
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    serverSocket = new ServerSocket(19000);
//                    s = serverSocket.accept();
//                    s.setReuseAddress(true);
//                    is = s.getInputStream();
//                    br = new BufferedReader(new InputStreamReader(is));
//
//
//                    while(true){
//
//                        if(s.isClosed()){
//                            break;
//                        }
//
//                        String str = br.readLine();
//
//                        if(!TextUtils.isEmpty(str)){
////                            System.out.println("\nreceived point: ");
////                            String str = new String(buf, 0, len);
//                            if(str.contains("end")){
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        System.out.println("moveend");
//                                        System.out.println("\n\n\n\n\n");
//                                        wrb.touch_up();
//                                        setMoveStart(true);
//                                    }
//                                });
//                                continue;
//                            }
//
//
//
////                            ArrayList<String> movePoints = readPoints(str);
//
//                            JSONObject jsonObject = null;
//                            System.out.println("\nstring is: " + str);
//                            try {
//                                jsonObject = new JSONObject(str);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            if(jsonObject==null){
//                                continue;
//                            }
//
//                            float w = Float.valueOf(jsonObject.getString("x"));
//                            float h = Float.valueOf(jsonObject.getString("y"));
//                            float fw = Float.valueOf(jsonObject.getString("frameWidth"));
//                            float fh = Float.valueOf(jsonObject.getString("frameHeight"));
//
//                            float wi = w*getResources().getDisplayMetrics().widthPixels/fw;
//                            float he = h*getResources().getDisplayMetrics().heightPixels/fh;
//
//                            final MovePoint mp = new MovePoint(wi, he);
//
////                            System.out.println("x: " + String.valueOf(mp.getX()) + "y: " + String.valueOf(mp.getY()) + "\n");
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if(isMoveStart()){
////                                            System.out.println("movestart");
//                                            setMoveStart(false);
//                                            wrb.touch_start(mp.getX(), mp.getY());
//                                        }
//                                        else{
////                                            System.out.println("move");
//                                            wrb.touch_move(mp.getX(), mp.getY());
//                                        }
//                                    }
//                                });
//
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//                finally {
//                        try {
//                            if(serverSocket!=null){
//                                serverSocket.close();
//                            }
//                            if(is!=null){
//                                is.close();
//                            }
//                            if(s!=null){
//                                s.close();
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                }
//            }
//        }.start();

    }
    private synchronized void sendObj(String eventName, Object... datas){
        if(socketClient ==null){
            socketClient = new SocketClient();
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
                                wrb.touch_up();
                            }
                        });
                    }
                    else if(str.contains("start")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                        System.out.println("movestart");
//                                        System.out.println("\n\n\n\n\n");
                                setMoveStart(true);
                            }
                        });
                    }
                    else if(str.contains("clear")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                wrb.clear();
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
                            wrb.setStrokeWidth(strokeWidth);
                        };

                        if(jsonObject.has("color")){
                            String color = jsonObject.getString("color");

                            wrb.setPaintColor(color);
                        }


    //                            System.out.println("x: " + String.valueOf(mp.getX()) + "y: " + String.valueOf(mp.getY()) + "\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isMoveStart()) {
    //                                            System.out.println("movestart");
                                    setMoveStart(false);
                                    wrb.touch_start(mp.getX(), mp.getY());
                                } else {
    //                                            System.out.println("move");
                                    wrb.touch_move(mp.getX(), mp.getY());
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
        try {
            if(is!=null){
                is.close();
            }
            if(socket!=null){
                socket.close();
            }
            if(br!=null){
                br.close();
            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        super.onDestroy();
    }
}
