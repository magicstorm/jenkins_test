package com.hgxx.whiteboard.entities;

import android.util.Log;

/**
 * Created by ly on 04/05/2017.
 */

public class ScrollStat {
    float currentHeight = 0;
    float totalHeight = 0;

    @Override
    public boolean equals(Object scrollStat) throws ClassCastException,NullPointerException{
        ScrollStat ss = (ScrollStat)scrollStat;
        return this.getCurrentHeight() == ss.getCurrentHeight() &&
                this.getTotalHeight() == ss.getTotalHeight();
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
}
