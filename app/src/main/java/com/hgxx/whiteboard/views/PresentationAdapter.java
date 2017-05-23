package com.hgxx.whiteboard.views;

import com.hgxx.whiteboard.models.PresentationInfo;

/**
 * Created by ly on 24/05/2017.
 */

public abstract class PresentationAdapter {
    public abstract int getCount();
    public abstract PresentationInfo getPresentationInfo(int pos);
}
