package com.hgxx.whiteboard.views.drawview;

/**
 * Created by ly on 11/05/2017.
 */

public interface DrawControl {
    interface DrawListener{
        void onDrawStart();
        void onDrawMove(float x, float y);
        void onDrawEnd();
    }
    void setDrawListener(DrawListener drawListener);
    String getDrawType();
    float getStrokeWidth();
    String getPaintColor();
    void clear();
    void undo();
    void setPaintColor(String color);
    void setStrokeWidth(float strokeWidth);
    void setDrawType(String drawType);

}
