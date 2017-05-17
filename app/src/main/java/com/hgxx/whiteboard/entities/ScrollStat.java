package com.hgxx.whiteboard.entities;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.hgxx.whiteboard.utils.JsonUtils;

/**
 * Created by ly on 04/05/2017.
 */

public class ScrollStat {
    String presentationName;
    String roomId;
    String presentationId;
    float currentHeight = 0;
    float totalHeight = 0;
    Display display;

    public ScrollStat(String presentationName, float currentHeight, float totalHeight){
        this.currentHeight = currentHeight;
        this.totalHeight = totalHeight;
        this.presentationName = presentationName;
    }

    @Override
    public boolean equals(Object scrollStat) throws ClassCastException,NullPointerException{
        ScrollStat ss = (ScrollStat)scrollStat;
        return this.getCurrentHeight() == ss.getCurrentHeight() &&
                this.getTotalHeight() == ss.getTotalHeight() ;
    }

    public ScrollStat computeLocalScrollStat(int screenHeight){
        currentHeight = currentHeight*screenHeight/totalHeight;
        totalHeight = screenHeight;
        return this;
    }

    public float getCurrentHeight() {
        return currentHeight;
    }

    public void setCurrentHeight(float currentHeight) {
        this.currentHeight = currentHeight;
    }

    public float getTotalHeight() {
        return totalHeight;
    }

    public void setTotalHeight(float totalHeight) {
        this.totalHeight = totalHeight;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPresentationId() {
        return presentationId;
    }

    public void setPresentationId(String presentationId) {
        this.presentationId = presentationId;
    }
}
