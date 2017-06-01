package com.hgxx.whiteboard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.entities.MovePoint;
import com.hgxx.whiteboard.entities.ScrollStat;
import com.hgxx.whiteboard.models.Presentation;
import com.hgxx.whiteboard.models.PresentationInfo;
import com.hgxx.whiteboard.network.constants.Sock;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;



/**
 * Created by ly on 16/05/2017.
 */

public class HgWhiteBoardRcv extends FrameLayout{
    private DrawViewController drawView;
    private ScrollView scrollView;
    private LinearLayout scrollLl;
    private Presentation presentation;

    private DrawLayout drawLayout;

    private String imageUrl;
    private String psId;
    private String roomId;
    private int presentationCount;
    private String presentationName;

    private PresentationAdapter presentationAdapter;
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public HgWhiteBoardRcv(@NonNull Context context) {
        this(context, null);
    }

    public HgWhiteBoardRcv(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HgWhiteBoardRcv(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttributes(context, attrs, defStyleAttr);
    }

     private void getAttributes(Context context, AttributeSet attributeSet, int defStyleAttr){
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.HgWhiteBoard, defStyleAttr, 0);
        String serverSocketAddress = ta.getString(R.styleable.HgWhiteBoard_serverSocketAddress);
        int serverSocketPort = ta.getInt(R.styleable.HgWhiteBoard_serverSocketPort, 8081);
        String serverSocketProtocol = ta.getString(R.styleable.HgWhiteBoard_serverSocketProtocol);

        String serverWebAddress = ta.getString(R.styleable.HgWhiteBoard_serverWebAddress);
        int serverWebPort = ta.getInt(R.styleable.HgWhiteBoard_serverWebPort, 443);
        String serverWebProtocol = ta.getString(R.styleable.HgWhiteBoard_serverWebProtocol);

        boolean customSocketServer = ta.getBoolean(R.styleable.HgWhiteBoard_customSocketServer, false);
        boolean customWebServer = ta.getBoolean(R.styleable.HgWhiteBoard_customWebServer, false);
        if(customSocketServer){
            Sock.protocol = serverSocketProtocol;
            Sock.serverPort = serverSocketPort;
            Sock.serverIP = serverSocketAddress;
        }
        if(customWebServer){
            Web.protocol = serverWebProtocol;
            Web.port = serverWebPort;
            Web.address = serverWebAddress;
        }
    }

    public void init(){
        inflate(getContext(), R.layout.activity_white_board_rcv, this);
        findViews();
        initDatas();
        initSocketClient();
    }

//    public void setPresentationId(String presentationId){
//        psId = presentationId;
//    }
//
//    public void setRoomId(String roomId){
//        this.roomId = roomId;
//    }
//
//    public void setPresentationCount(int count){
//        presentationCount = count;
//    }
//
//    public void setPresentationName(String presentationName){
//        this.presentationName = presentationName;
//    }





    private void initDatas(){
        drawView = new DrawViewController(drawLayout);
        drawView.setDrawable(false);
        presentation = new Presentation(presentationAdapter.getPresentationName());
        presentation.setRoomId(presentationAdapter.getRoomId());
        presentation.setPresentationId("0");
        presentation.setPresentationCount(presentationAdapter.getPresentationInfo(0).getCount());
    }


    private void initViews(final Display display){

//        adjustDisplayArea(display.computeLocalDisplaySize(getContext()));

        drawView.setWidth((int)display.getDisplayWidth());
        drawView.setHeight((int)display.getDisplayHeight());
        drawView.clear();


        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);


        presentation.setTotalWidth(drawView.getWidth());
        presentation.setPresentationFrame(scrollLl);
        presentation.loadPresentation(getContext(), imageUrl, new Presentation.OnLoadPresentationCallBack() {
            @Override
            public void onLoadPresentationCompleted() {
                drawView.setHeight(presentation.getTotalHeight());

                presentation.getScrollStat().computeLocalScrollStat(presentation.getTotalHeight());

                presentation.setScrollStat(presentation.getScrollStat());


            }



            @Override
            public void onNext(Integer integer) {

            }

        }, presentationAdapter);

        presentation.setOnScrollStatChangeListener(new Presentation.OnScrollStatChange() {
            //int i = 0;
            @Override
            public void onScrollStatChange(ScrollStat scrollStat){
                int scrollTop = (int) scrollStat.getCurrentHeight();

                if(presentation.getActHeight()==0)return;
                int curPage = scrollTop/presentation.getActHeight();


                if((presentation.getCurrentPage()-1)!=curPage){
                    if(!presentation.getPresentationId().equals("-1")){
                        presentation.removeImages(curPage);
                        presentation.loadNearByImages(getContext(), curPage, getPresentationAdapter());
                    }
                    presentation.setCurrentPage(curPage+1);
                }

                scrollView.scrollTo(0, scrollTop);
            }
        });

        presentation.listenPresentationChange(getContext());

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


    public void close(){
        this.setVisibility(GONE);
    }

    public void open(){
        this.setVisibility(VISIBLE);

    }


    private void initSocketClient() {
        presentation.initClient(new Presentation.EventObserver() {
            @Override
            public void onPresentationInit(final ScrollStat scrollStat) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        boolean fuck = scrollStat.getPresentationId().equals(presentation.getPresentationId());
                        PresentationInfo pi = null;
                        if(scrollStat.getPresentationId().equals(Presentation.PRESENTATION_TYPE_WHITEBOARD)){
                            pi = new PresentationInfo("wb");
                            pi.setPresentationId("-1");
                            pi.setSizeRatio(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().heightPixels);
                            if(pi!=null){
                                setImageUrl(null);
                                presentation.setPresentationId(pi.getPresentationId());

                                int height = getResources().getDisplayMetrics().heightPixels;
                                presentation.setTotalHeight(height);
                                scrollStat.computeLocalScrollStat(presentation.getTotalHeight());
                                scrollStat.getDisplay().computeLocalDisplaySize(getContext());
                                scrollStat.getDisplay().setDisplayHeight(presentation.getTotalHeight());;
                                scrollStat.getDisplay().setDisplayWidth(presentation.getTotalWidth());
                            }

                        }
                        else /*if(!scrollStat.getPresentationId().equals(presentation.getPresentationId()))*/{
                            pi = presentationAdapter.getPresentationInfo(scrollStat.getPresentationId());
                            if(pi!=null){
                                setImageUrl(pi.getUrl());
                                presentation.setPresentationId(pi.getPresentationId());
                                presentation.setPresentationName(pi.getPresentationName());

                                if(presentation.getTotalHeight()!=0){
                                    scrollStat.computeLocalScrollStat(presentation.getTotalHeight());
                                }
                                scrollStat.getDisplay().computeLocalDisplaySize(getContext());
                                scrollStat.getDisplay().setDisplayHeight(scrollStat.getTotalHeight());;
                                int width = getResources().getDisplayMetrics().widthPixels;
                                presentation.setTotalWidth(width);
                                scrollStat.getDisplay().setDisplayWidth(width);
                            }
                        }



                        open();

//                        setImageUrl(imageUrl);
//                        init();

                        presentation.setSizeRatio(pi.getSizeRatio());
                        if(!scrollStat.getPresentationId().equals(Presentation.PRESENTATION_TYPE_WHITEBOARD)){
                            presentation.setPresentationCount(presentationAdapter.getPresentationInfo(scrollStat.getPresentationId()).getCount());
                        }

                        presentation.setScrollStat(scrollStat);
                        initViews(scrollStat.getDisplay());
                    }
                });
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

            @Override
            public void onEnd(String id) {
                if(onEndSession!=null){
                    onEndSession.onEndSession();
                }
            }

            @Override
            public void onClose(String presentationId) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        close();
                    }
                });
            }
        });
    }


    public interface OnEndSession{
        void onEndSession();
    }

    private OnEndSession onEndSession;

    public void setOnEndSession(OnEndSession onEndSession) {
        this.onEndSession = onEndSession;
    }

    public PresentationAdapter getPresentationAdapter() {
        return presentationAdapter;
    }

    public void setPresentationAdapter(PresentationAdapter presentationAdapter) {
        this.presentationAdapter = presentationAdapter;
    }
}



