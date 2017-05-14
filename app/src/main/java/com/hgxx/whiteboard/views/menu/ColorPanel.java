package com.hgxx.whiteboard.views.menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ly on 12/05/2017.
 */

public class ColorPanel extends LinearLayout{

    private int indicatorRaduis = dp2px(20);
    private ArrayList<String> colorArr = new ArrayList<>();

    public interface OnColorSelected{
        void onColorSelected(String color);
    }
    private OnColorSelected onColorSelected;

    public void setOnColorSelected(OnColorSelected onColorSelected) {
        this.onColorSelected = onColorSelected;
    }

    public ColorPanel(Context context) {
        this(context, null);
    }

    public ColorPanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initColors();
        initIndicators();
    }

    private void initColors(){
        colorArr.add("#999999");
        colorArr.add("#ff0033");
        colorArr.add("#00cc00");
        colorArr.add("#0000cc");
        colorArr.add("#ffcc00");
        colorArr.add("#ff6633");
        colorArr.add("#990099");
        colorArr.add("#663300");
        colorArr.add("#000000");
    }

    private void initIndicators(){
        for(String color: colorArr){
            this.addView(genColorIndicator(color));
        }
    }

    private RelativeLayout genColorIndicator(final String color){
        OvalShape ovalShape = new OvalShape();
        ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint().setColor(Color.parseColor(color));


        RelativeLayout indicatorFrame = new RelativeLayout(getContext());
        LayoutParams indicatorFrameParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        indicatorFrameParams.weight = 1;
        indicatorFrame.setLayoutParams(indicatorFrameParams);

        indicatorFrame.setClickable(true);
        indicatorFrame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onColorSelected!=null){
                    onColorSelected.onColorSelected(color);
                }
            }
        });


        FrameLayout indicator = new FrameLayout(getContext());

        RelativeLayout.LayoutParams indicatorLayoutParams = new RelativeLayout.LayoutParams(indicatorRaduis*2, indicatorRaduis*2);
        indicatorLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        indicator.setLayoutParams(indicatorLayoutParams);


        final int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            indicator.setBackgroundDrawable(shapeDrawable);
        } else {
            indicator.setBackground(shapeDrawable);
        }

        indicatorFrame.addView(indicator);
        return indicatorFrame;
    }

    private int dp2px(float dp){
        return (int)(getContext().getResources().getDisplayMetrics().density*dp+0.5f);
    }

    public void setIndicatorRaduis(int indicatorRaduis) {
        this.indicatorRaduis = indicatorRaduis;
    }

    public int getIndicatorRaduis() {
        return indicatorRaduis;
    }
}
