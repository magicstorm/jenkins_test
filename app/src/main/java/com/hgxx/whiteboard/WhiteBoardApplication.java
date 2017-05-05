package com.hgxx.whiteboard;

import android.app.Application;
import android.content.Context;

/**
 * Created by ly on 05/05/2017.
 */

public class WhiteBoardApplication extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext(){
        return mContext;
    }
}
