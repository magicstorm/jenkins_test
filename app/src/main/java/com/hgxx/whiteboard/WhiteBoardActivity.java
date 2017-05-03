package com.hgxx.whiteboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.hgxx.whiteboard.constants.Sock;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.views.drawview.DrawView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

public class WhiteBoardActivity extends AppCompatActivity {

    private DrawView wb;
    private Socket socket;
    private SocketClient socketClient;
    private int connectionId = -1;


    public synchronized void setSocketIfNoSocket() throws IOException {
        if(socket==null){
            socket = new Socket(Sock.serverIP, Sock.serverPort);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wb = new DrawView(this);
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        RelativeLayout.LayoutParams Params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        RelativeLayout rl = new RelativeLayout(this);
//        rl.setLayoutParams(layoutParams);
//        rl.addView(wb);

//        wb.setLayoutParams(layoutParams);

        setContentView(wb);

        //clear btn
        Button clear = new Button(this);
        clear.setText("Clear");
        clear.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wb.clear();
                socketClient.sendEvent(SocketClient.EVENT_SIG, "clear");
            }
        });

        //color btns
//        Button green = new Button(this);
//        green.setText("Green");
//        green.layout(clear.getLeft()+10, clear.getTop(), clear.getRight()+10, clear.getBottom());
//
//        green.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
//        green.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                wb.setPaintColor("00ff00");
//
//            }
//        });


        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin=(int)(80*getResources().getDisplayMetrics().density);
        clear.setLayoutParams(layoutParams);

        ((ViewGroup)getWindow().getDecorView()).addView(clear);
        clear.bringToFront();




        //socket test
//        MovePoint p = new MovePoint(10, 20);
//        p.setFrameHeight(100);
//        p.setStrokeWidth(200);
//        p.setStrokeWidth(3);
//        p.setColor("#ff0000");
//        p.setIndex(1);
//        p.setTimestamp(System.currentTimeMillis());
//        sendPoint(p);


        sendObj(SocketClient.EVENT_SIG, "server");


//        sendString("server\n");


        wb.setOnMoveListener(new DrawView.OnMoveListener() {
            @Override
            public void onMoveStart() {
                sendObj(SocketClient.EVENT_SIG, "start");
            }

            @Override
            public void onMove(float x, float y) {
                //TODO send path
                MovePoint mp = new MovePoint(x, y);
                mp.setFrameWidth(wb.getCurWidth());
                mp.setFrameHeight(wb.getCurHeight());
                sendObj(SocketClient.EVENT_PATH, mp.toString());
            }

            @Override
            public void onMoveEnd() {
                sendObj(SocketClient.EVENT_SIG, "end");
            }
        });
    }


    private synchronized void sendObj(String eventName, Object... datas){
        if(socketClient==null){
            socketClient = new SocketClient();
            socketClient.setEventListener(SocketClient.EVENT_CONNECTION, new SocketClient.EventListener() {
                @Override
                public void onEvent(Object... args) {
                    try {
                    connectionId = Integer.valueOf(((JSONObject)args[0]).getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    System.out.println((String)args[0]);
                }
            });
        }
        if(!socketClient.isConnected()){
            socketClient.connect();
        }

        socketClient.sendEvent(eventName, datas);
    }
//
//    private void sendPoint(MovePoint mp){
//        JSONObject pointJson = obj2json(mp);
//        System.out.println("send point: " + String.valueOf(mp.getX()) + String.valueOf(mp.getY()));
//        sendString(pointJson.toString()+"\n");
//    }
//
//    private synchronized void sendString(final String str) {
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    setSocketIfNoSocket();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if(TextUtils.isEmpty(str))return;
//                OutputStream os=null;
//                try {
//                    if(socket!=null){
//                        os = socket.getOutputStream();
//                        os.write(str.getBytes());
//                    }
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }



    @Override
    protected void onDestroy() {
        socketClient.disconnect();
        socketClient.close();
        super.onDestroy();
    }
}
