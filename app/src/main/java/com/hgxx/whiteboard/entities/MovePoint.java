package com.hgxx.whiteboard.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by ly on 27/04/2017.
 */

public class MovePoint {
    float frameWidth;
    float frameHeight;

    public MovePoint(float x, float y){
        this.x = x;
        this.y = y;
        timestamp = System.currentTimeMillis();
    }

    private String color;
    private float strokeWidth;


    private float x;
    private float y;
    private long timestamp;
    private long index;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }
    public float getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
    }

    public float getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field: fields){
            field.setAccessible(true);
            try {
                jsonObject.put(field.getName(), field.get(this));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        return jsonObject.toString();
    }
}
