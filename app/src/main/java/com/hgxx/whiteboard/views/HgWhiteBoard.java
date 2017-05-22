package com.hgxx.whiteboard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.models.Presentation;
import com.hgxx.whiteboard.network.constants.Sock;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.utils.ViewHelpers;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;
import com.hgxx.whiteboard.views.menu.ColorPanel;
import com.hgxx.whiteboard.views.menu.MenuBarController;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by ly on 16/05/2017.
 */

public class HgWhiteBoard extends FrameLayout {

    public static final String OPERATION_MODE_CUST = "custom";
    private HgScrollView scrollView;
    private DrawLayout drawLayout;
    private DrawViewController drawView;
    private LinearLayout docll;
    private DisplayMetrics dm;
    private int screenWidth;
    private int screenHeight;
    private Presentation presentation;

    private static WeakReference<DrawViewController> drawViewWeakReference;
    private static WeakReference<Presentation> presentationWeakReference;
    private static WeakReference<HgScrollView> scrollViewWeakReference;
    private static WeakReference<HgWhiteBoard> selfWeakReference;
    private LinearLayout menull;
    private ColorPanel colorPanel;
    private LinearLayout widthPanel;
    private LinearLayout shapePanel;
    private TextView pageNumTv;
    private EditText pageEt;
    private TextView pageBtn;


    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public HgWhiteBoard(@NonNull Context context) {
        this(context, null);
    }

    public HgWhiteBoard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HgWhiteBoard(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
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
        inflate(getContext(), R.layout.activity_white_board, this);
        findViews();
        initDatas();
        initViews();

    }
    private void initDatas(){
        dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

        presentation = new Presentation("Test");
        presentationWeakReference = new WeakReference<>(presentation);
        presentation.setPresentationFrame(docll);
        selfWeakReference = new WeakReference<>(this);

        presentation.setPresentationId("1");
        presentation.setRoomId("1");

    }

    static class OnPresentationLoaded implements Presentation.OnLoadPresentationCallBack{

        private DrawViewController drawView;
        private Presentation presentation;
        private HgWhiteBoard whiteBoard;

        public OnPresentationLoaded(){
            this.drawView = drawViewWeakReference.get();
            this.presentation = presentationWeakReference.get();
            this.whiteBoard = selfWeakReference.get();
        }

        @Override
        public void onLoadPresentationCompleted() {

            if(drawView==null||presentation==null||whiteBoard==null)return;
            try {
                presentation.initServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            presentation.initDrawMessage(drawView);

//            whiteBoard.pageNumTv.setText(presentation.getCurrentPage() + "/" + presentation.getPresentationCount());
            whiteBoard.scrollView.setOnScrollListener(new HgScrollView.OnScrollListener() {
                @Override
                public void onScrollChanged(int top, int oldt) {
                    int curPage = presentation.computeCurrentPage(top, oldt);
                    presentation.setCurrentPage(curPage);

                    //TODO display currentPage
//                    whiteBoard.pageNumTv.setText(curPage + "/" + presentation.getPresentationCount());

//                    ToastSingle.showCenterToast("current page: " + curPage, Toast.LENGTH_SHORT);

                    presentation.sendScroll(top);
                }
            });

//            whiteBoard.pageBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String pageNum = whiteBoard.pageEt.getText().toString();
//                    int no = Integer.parseInt(pageNum);
//                    if(no<=presentation.getPresentationCount()&&no>=1){
//                        int curTop = presentation.getPagePositions().get(no-1);
//                        whiteBoard.scrollView.scrollTo(0, curTop);
//                        presentation.sendScroll(curTop);
//                    }
//                }
//            });


            MenuBarController menuBarController = new MenuBarController(whiteBoard.getContext(), whiteBoard.menull);
            menuBarController.setDrawControl(drawView);
            menuBarController.setOnBtnClick(new MenuBarController.OnMenuBtnClick() {
                @Override
                public void onClear() {
                    presentation.clearPaint();
                }

                @Override
                public void onUndo() {
                    presentation.undoPaint();
                }
            });

            menuBarController.setColorPanel(whiteBoard.colorPanel);
            menuBarController.setWidthPanel(whiteBoard.widthPanel);
            menuBarController.setShapePanel(whiteBoard.shapePanel);;


            menuBarController.init();



        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Integer integer) {
        }
    }

    private void initViews(){
        docll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int docWidth = docll.getWidth();
                scrollView.addExcludedRectFs(new RectF(0, 0, docWidth, screenHeight));
            }
        });

        //init controller
        drawView = new DrawViewController(drawLayout);
        drawViewWeakReference = new WeakReference<>(drawView);
        drawView.setDrawable(true);

        scrollViewWeakReference = new WeakReference<>(scrollView);
        int displayWidth = screenWidth - ViewHelpers.dp2px(101, getContext());
        presentation.setTotalWidth(displayWidth);

        try {
            presentation.initPresentation(displayWidth, screenHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }

        presentation.loadPresentation(getContext(), imageUrl, new OnPresentationLoaded());

    }


    private void findViews(){
        scrollView = (HgScrollView)findViewById(R.id.sv);
        drawLayout = (DrawLayout)findViewById(R.id.draw_sender_view);
        docll = (LinearLayout)findViewById(R.id.doc_ll);
        menull = (LinearLayout)findViewById(R.id.menu);
        colorPanel = (ColorPanel)findViewById(R.id.color_panel);
        widthPanel = (LinearLayout)findViewById(R.id.width_panel);
        shapePanel = (LinearLayout)findViewById(R.id.shape_panel);
//        pageNumTv = (TextView)findViewById(R.id.page_number_tv);
//        pageEt = (EditText)findViewById(R.id.page_to_go);
//        pageBtn = (TextView)findViewById(R.id.page_btn);
    }

}
