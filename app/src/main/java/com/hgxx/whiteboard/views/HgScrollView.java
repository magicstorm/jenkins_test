package com.hgxx.whiteboard.views;

import android.content.Context;
import android.graphics.Matrix;
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
        if(isExcluded(excludedRectF))return;
        this.excludedRectFs.add(excludedRectF);
    }

    public void clearExcludedRects(){
        this.excludedRectFs.clear();
    }

    private boolean isExcluded(RectF newRectf){
        boolean isExcluded = false;
        for(RectF rectF:excludedRectFs){
            if(rectF.contains(newRectf)){
                isExcluded = true;
            }
        }
        return isExcluded;
    }

    public void setExcludedRectFs(ArrayList<RectF> excludedRectFs) {
        this.excludedRectFs = excludedRectFs;
    }

    public interface OnScrollListener{
        void onScrollChanged(int top, int oldt);
    }

    private OnScrollListener onScrollListener;

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(onScrollListener!=null){
            onScrollListener.onScrollChanged(t, oldt);
        }
    }
}
