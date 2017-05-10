package com.hgxx.whiteboard.entities;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by ly on 07/05/2017.
 */

public class Display {
    float displayWidth = 0;
    float displayHeight = 0;

    public Display(float displayWidth, float displayHeight){
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    public Display computeLocalDisplaySize(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        float dstRatio = displayHeight/displayWidth;
        float localRatio = screenHeight/(float)screenWidth;
        if(dstRatio>localRatio){
            displayWidth = screenHeight / dstRatio;
            displayHeight = screenHeight;
        }
        else{
            displayHeight = screenWidth * dstRatio;
            displayWidth = screenWidth;
        }
        return this;
    }

    public float getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(float displayWidth) {
        this.displayWidth = displayWidth;
    }

    public float getDisplayHeight() {
        return displayHeight;
    }

    public void setDisplayHeight(float displayHeight) {
        this.displayHeight = displayHeight;
    }


    @Override
    public boolean equals(Object obj) {
        Display ss = (Display)obj;
        return this.getDisplayHeight() == ss.getDisplayHeight() &&
                this.getDisplayWidth() == ss.getDisplayWidth();
    }

}

