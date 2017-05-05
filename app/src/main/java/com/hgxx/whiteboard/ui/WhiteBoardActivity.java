package com.hgxx.whiteboard.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.hgxx.whiteboard.network.constants.Sock;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.views.drawview.DrawView;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

public class WhiteBoardActivity extends AppCompatActivity {

    private DrawView wb;
    private SocketClient socketClient;
    private int connectionId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wb = new DrawView(this);


        setContentView(wb);

        final DrawViewController drawView= new DrawViewController(wb);
        //clear btn
        Button clear = new Button(this);
        clear.setText("Clear");
        clear.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clear();
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






        sendObj(SocketClient.EVENT_SIG, "server");





        wb.setOnMoveListener(new DrawView.OnMoveListener() {
            @Override
            public void onMoveStart() {
                sendObj(SocketClient.EVENT_SIG, "start");
            }

            @Override
            public void onMove(float x, float y) {
                //TODO send path
                MovePoint mp = new MovePoint(x, y);
                mp.setFrameWidth(drawView.getWidth());
                mp.setFrameHeight(drawView.getHeight());
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
            socketClient = SocketClient.getInstance();
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




    @Override
    protected void onDestroy() {
        socketClient.disconnect();
        socketClient.close();
        super.onDestroy();
    }
}
