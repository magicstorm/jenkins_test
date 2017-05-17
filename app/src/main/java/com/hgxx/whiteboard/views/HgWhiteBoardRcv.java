package com.hgxx.whiteboard.views;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.WhiteBoardApplication;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.models.Presentation;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

import java.util.ArrayList;


/**
 * Created by ly on 16/05/2017.
 */

public class HgWhiteBoardRcv extends FrameLayout{
    private DrawViewController drawView;
    private ScrollView scrollView;
    private LinearLayout scrollLl;
    private Presentation presentation;

    private DrawLayout drawLayout;

    public HgWhiteBoardRcv(@NonNull Context context) {
        this(context, null);
    }

    public HgWhiteBoardRcv(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HgWhiteBoardRcv(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.activity_white_board_rcv, this);
        findViews();
        initDatas();
        initSocketClient();
    }

    private void initDatas(){
        drawView = new DrawViewController(drawLayout);
        drawView.setDrawable(false);
        presentation = new Presentation("Test");
        presentation.setRoomId("1");
    }


    private void initViews(Display display){

        adjustDisplayArea(display.computeLocalDisplaySize(WhiteBoardApplication.getContext()));

        drawView.setWidth((int)display.getDisplayWidth());
        drawView.setHeight((int)display.getDisplayHeight());
        drawView.clear();


        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);

        presentation.setTotalWidth(drawView.getWidth());
        presentation.setPresentationFrame(scrollLl);
        presentation.loadPresentation(getContext(), new Presentation.OnLoadPresentationCallBack() {
            @Override
            public void onLoadPresentationCompleted() {
                drawView.setHeight(presentation.getTotalHeight());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {

            }
        });

        presentation.setOnScrollStatChangeListener(new Presentation.OnScrollStatChange() {
            //int i = 0;
            @Override
            public void onScrollStatChange(ScrollStat scrollStat){
                final int scrollTop = (int) scrollStat.getCurrentHeight();
                scrollView.scrollTo(0, scrollTop);
            }
        });

        presentation.listenPresentationChange(WhiteBoardApplication.getContext());

    }

    private void adjustDisplayArea(final Display display){
//        int sw = getResources().getDisplayMetrics().widthPixels;
//        int sh = getResources().getDisplayMetrics().heightPixels;
//        int ww = (int)display.getDisplayWidth();

        presentation.setTotalHeight((int)display.getDisplayHeight());
        scrollView.getLayoutParams().width = (int)display.getDisplayWidth();
        presentation.setTotalWidth((int)display.getDisplayWidth());
        scrollView.invalidate();
    }


    private void findViews(){
        scrollView = (ScrollView)findViewById(R.id.sv);
        scrollLl = (LinearLayout) findViewById(R.id.ll);
        drawLayout = (DrawLayout)findViewById(R.id.drawRcvView);
    }

    private void initSocketClient() {
        presentation.initClient(new Presentation.EventObserver() {
            @Override
            public void onPresentationInit(ScrollStat scrollStat) {
                initViews(scrollStat.getDisplay());
            }

            @Override
            public void onReceiveSignal(String str) {
                if (str.contains("end")) {
                    drawView.drawEnd();
                }
                else if(str.contains("start")){
                    drawView.setMoving(true);
                }
                else if(str.contains("clear")){
                    drawView.clear();
                }
                else if(str.contains("undo")){
                    drawView.undo();
                }

            }

            @Override
            public void onMove(MovePoint movePoint) {
                drawView.setStrokeWidth(movePoint.getStrokeWidth());
                drawView.setPaintColor(movePoint.getPaintColor());
                drawView.setDrawType(movePoint.getDrawType());

                if (drawView.isMoving()) {
                    drawView.setMoving(false);
                    drawView.startDraw(movePoint.getX(), movePoint.getY());
                } else {
                    drawView.drawMove(movePoint.getX(), movePoint.getY());
                }
            }

            @Override
            public void onConnection(String id) {
            }
        });
    }

}


