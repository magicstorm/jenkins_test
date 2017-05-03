package com.hgxx.whiteboard.network;

import com.hgxx.whiteboard.WhiteBoardActivity;
import com.hgxx.whiteboard.constants.Sock;
import com.hgxx.whiteboard.models.ChatObject;
import com.hgxx.whiteboard.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.emitter.Emitter.Listener;

/**
 * Created by ly on 02/05/2017.
 */

public class SocketClient {

    public static final String EVENT_SIG = "sig";
    public static final String EVENT_DATA = "data";
    public static final String EVENT_CONNECTION = "connection";
    public static final String EVENT_PATH = "path";


    private String serverUri;
    private Socket socket;

    public SocketClient(){
        try {
            serverUri = Sock.protocol + "://" + Sock.serverIP + ":" + String.valueOf(Sock.serverPort);
            socket = IO.socket(serverUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public interface EventListener{
        void onEvent(Object... args);
    }

    public void setEventListener(String eventName, EventListener eventListener){
        if(socket==null)return;
        socket.on(eventName, new EListener(eventListener));
    }

    public void sendEvent(String eventName, Object... datas){
        if(socket==null)return;
        socket.emit(eventName, datas);
    }


    class EListener implements Listener {
        EventListener eventListener;
        public EListener(EventListener eventListener){
            this.eventListener = eventListener;
        }

        @Override
        public void call(Object... args) {
            eventListener.onEvent(args);
        }
    }

    public void connect(){
        if(socket==null)return;
        socket.connect();
    }


    public void disconnect(){
        if(socket==null)return;
        socket.disconnect();
    }

    public boolean isConnected(){
        return socket.connected();
    }

    public void close(){
        if(socket==null)return;
        socket.close();
    }


    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }
}
