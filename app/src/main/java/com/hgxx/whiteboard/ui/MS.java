package com.hgxx.whiteboard.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.hgxx.whiteboard.entities.ScrollStat;

/**
 * Created by ly on 08/05/2017.
 */

public class MS extends ScrollView{
    public MS(Context context) {
        this(context, null);
    }

    public MS(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MS(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

}
