package com.hgxx.whiteboard.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hgxx.whiteboard.WhiteBoardApplication;


public class ToastSingle {
    private static Toast toast = null;
    private static Context mContext;
    private static TextView textView;

    public static void showToast(Context context, String s, int duration) {
        if (toast == null) {
            toast = Toast.makeText(context, s, duration);
            toast.show();
        } else {
            toast.setText(s);
            toast.show();
        }
    }

    public static void showToastCenter(Context context, String s){
        if(toast==null){
            toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        else{
            toast.setText(s);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private static Toast centerToast = null;

    //中间显示的toast
    public static void showCenterToast(String msg, int duration) {
        if (centerToast == null) {
            mContext = WhiteBoardApplication.getContext();
            centerToast = Toast.makeText(mContext, null, duration);
            centerToast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout layout = (LinearLayout) centerToast.getView();
//            layout.setBackgroundResource(R.drawable.textview_toast_bg);
            layout.setBackgroundColor(Color.parseColor("#c0000000"));
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER);
            textView = new TextView(mContext);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.parseColor("#ffffffff"));
            textView.setTextSize(14);
            int i = ViewHelpers.dpToPixels(12, mContext);
            int j =ViewHelpers.dpToPixels(6, mContext);
            textView.setPadding(i, j, i, j);
            textView.setText(msg);
            layout.addView(textView);
            centerToast.show();
        } else {
            textView.setText(msg);
            centerToast.show();
        }
    }
}