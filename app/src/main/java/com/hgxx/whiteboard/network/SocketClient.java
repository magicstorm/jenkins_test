package com.hgxx.whiteboard.network;

import com.hgxx.whiteboard.network.constants.Sock;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;

/**
 * Created by ly on 02/05/2017.
 */

public class SocketClient {

    public static final String EVENT_SIG = "sig";
    public static final String EVENT_DATA = "data";
    public static final String EVENT_CONNECTION = "connection";
    public static final String EVENT_PATH = "path";
    public static final String EVENT_PRESENTATION = "presentation";
    public static final String EVENT_PRESENTATION_INIT = "presentation_init";
    public static final String EVENT_PRESENTATION_REQUEST = "presentation_request";


    private String serverUri;
    private Socket socket;

    private static SocketClient socketClient;
    private SocketClient(){
        try {
            if(socket==null){
                composeServerUrl();
                socket = IO.socket(serverUri);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void composeServerUrl() {
        serverUri = (Sock.protocol + "://" + Sock.serverIP + ":" + String.valueOf(Sock.serverPort));
    }

    public static synchronized SocketClient getInstance(){
        if(socketClient==null){
            socketClient = new SocketClient();
        }
        return socketClient;
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
