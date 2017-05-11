package com.hgxx.whiteboard.ui;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.views.HgScrollView;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by ly on 09/05/2017.
 */

public class WhiteBoardActivity extends AppCompatActivity{

    private HgScrollView scrollView;
    private DrawLayout drawLayout;
    private DrawViewController drawView;
    private LinearLayout docll;
    private DisplayMetrics dm;
    private int screenWidth;
    private int screenHeight;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board);
        initDatas();
        findViews();
        initViews();

        drawView = new DrawViewController(drawLayout);
        drawView.setDrawable(true);

        initSocketClient();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initSocketClient() {

    }

    private void initDatas(){
        dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
    }

    private void initViews(){
        docll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int docWidth = docll.getWidth();
                scrollView.addExcludedRectFs(new RectF(0, 0, docWidth, screenHeight));
            }
        });
    }

    private void findViews(){
        scrollView = (HgScrollView)findViewById(R.id.sv);
        drawLayout = (DrawLayout)findViewById(R.id.draw_sender_view);
        docll = (LinearLayout)findViewById(R.id.doc_ll);
    }


//    private boolean isJsonFieldNotNull(JSONObject jsonObject, String key) throws JSONException {
//        return jsonObject.has(key)&&!TextUtils.isEmpty(jsonObject.getString(key))&&!jsonObject.getString(key).equals("null");
//    }
}
