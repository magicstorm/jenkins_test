package com.hgxx.whiteboard;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by ly on 05/05/2017.
 */

public class WhiteBoardApplication extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Fresco.initialize(this);
    }

    public static Context getContext(){
        return mContext;
    }
}
