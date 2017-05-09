package com.hgxx.whiteboard.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.network.SocketClient;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ly on 09/05/2017.
 */

public class WhiteBoardActivity extends AppCompatActivity{

    private ScrollView scrollView;
    private LinearLayout scrollLl;
    private DrawLayout drawLayout;
    private DrawViewController drawView;
    private SocketClient socketClient;
    private Integer connectionId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board);
        findViews();

        drawView = new DrawViewController(drawLayout);
        drawView.setDrawable(true);

        initSocketClient();
    }

    private void initSocketClient() {
        if(socketClient ==null){
            socketClient = SocketClient.getInstance();

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
        if(!socketClient.isConnected()){
            socketClient.connect();
        }
    }

    private void findViews(){
        scrollView = (ScrollView)findViewById(R.id.sv);
        scrollLl = (LinearLayout) findViewById(R.id.ll);
        drawLayout = (DrawLayout)findViewById(R.id.drawRcvView);
    }

    private boolean isJsonFieldNotNull(JSONObject jsonObject, String key) throws JSONException {
        return jsonObject.has(key)&&!TextUtils.isEmpty(jsonObject.getString(key))&&!jsonObject.getString(key).equals("null");
    }
}
