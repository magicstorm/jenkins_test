package com.hgxx.whiteboard.views;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import java.util.ArrayList;

/**
 * Created by ly on 11/05/2017.
 */

public class HgScrollView extends ScrollView {

    private ArrayList<RectF> excludedRectFs = new ArrayList<>();

    public HgScrollView(Context context) {
        this(context, null);
    }

    public HgScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HgScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float cx = ev.getX();
        float cy = ev.getY();
        for(RectF rectf: getExcludedRectFs()){
            if(rectf.contains(cx, cy)){
                return false;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public ArrayList<RectF> getExcludedRectFs() {
        return excludedRectFs;
    }

    public void addExcludedRectFs(RectF excludedRectF) {
        this.excludedRectFs.add(excludedRectF);
    }

    public void setExcludedRectFs(ArrayList<RectF> excludedRectFs) {
        this.excludedRectFs = excludedRectFs;
    }
}
