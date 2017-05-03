package com.hgxx.whiteboard.views.drawview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by ly on 03/05/2017.
 */

public class DrawViewControllPanel extends LinearLayout{
    public DrawViewControllPanel(Context context) {
        this(context, null);
    }

    public DrawViewControllPanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawViewControllPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
