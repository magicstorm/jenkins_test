package com.hgxx.whiteboard.network.Models;

/**
 * Created by ly on 02/05/2017.
 */

public class Signal {
    private static String id;
    private String sig;
    public Signal(String sig){
        this.sig = sig;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        Signal.id = id;
    }
}
