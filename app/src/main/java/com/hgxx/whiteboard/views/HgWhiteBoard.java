package com.hgxx.whiteboard.views;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.models.Presentation;
import com.hgxx.whiteboard.models.PresentationInfo;
import com.hgxx.whiteboard.network.constants.Sock;
import com.hgxx.whiteboard.network.constants.Web;
import com.hgxx.whiteboard.utils.ViewHelpers;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;
import com.hgxx.whiteboard.views.menu.MenuBarController;
import com.hgxx.whiteboard.views.menu.ShapePanel;
import com.hgxx.whiteboard.views.menu.TopBarController;

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
    private ColorPopoutMenu colorPanel;
    private WidthPanel widthPanel;
    private ShapePanel shapePanel;
    private PopoutMenu progressPanel;
    private TextView pageNumTv;
    private EditText pageEt;
    private TextView pageBtn;

    private boolean seek=false;
    private boolean scrolling=false;
    private boolean startUp=true;

    private String imageUrl;
    private RelativeLayout topBar;
    private ColorPointer colorPointer;
    private RectF displayRect;
    private SeekBar scrollSeekBar;
    private ImageView closeBtn;
    private int displayWidth;
    private FrameLayout chooseFl;
    private PresentationSelectFragment chooseFragment;

    private String sesstionTitle;
    private PresentationAdapter presentationAdapter;
    private WeakReference<Activity> activityWeakReference;
    private ImageView choosePresentationBtn;
    private static MenuBarController menuBarController;

    public String getSesstionTitle() {
        return sesstionTitle;
    }

    public void setSesstionTitle(String sesstionTitle) {
        this.sesstionTitle = sesstionTitle;
    }

    public PresentationAdapter getPresentationAdapter() {
        return presentationAdapter;
    }

    public void setPresentationAdapter(PresentationAdapter presentationAdapter) {
        this.presentationAdapter = presentationAdapter;
    }

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


    public void initPresInfo(Activity activity){
        inflate(getContext(), R.layout.activity_white_board, this);
        findViews();
        activityWeakReference = new WeakReference<>(activity);
        initChoosePresentationFragment(false);
    }

    private void initChoosePresentationFragment(boolean canReturn){

        presentation = new Presentation(presentationAdapter.getPresentationName());
        presentationWeakReference = new WeakReference<>(presentation);
        presentation.setRoomId(presentationAdapter.getRoomId());

        chooseFl.setVisibility(VISIBLE);
        chooseFl.bringToFront();
        Activity activity = activityWeakReference.get();
        if(activity==null)return;

        chooseFragment = new PresentationSelectFragment();
        chooseFragment.setPresentationAdapter(presentationAdapter);
        chooseFragment.setTitle(sesstionTitle);
        chooseFragment.setCanReturn(canReturn);
        chooseFragment.setOnPresentationSelectPageClose(new PresentationSelectFragment.OnPresentationSelectPageClose() {
            @Override
            public void onPresentationSelected(PresentationInfo pi) {
                //TODO set initial presentation params
                if(drawView!=null){
                    drawView.clear();
                }
                chooseFl.setVisibility(GONE);
                setImageUrl(pi.getUrl());
                presentation.setPresentationId(pi.getPresentationId());
                presentation.setPresentationCount(pi.getCount());
                if(presentation.getScrollStat()!=null){
                    presentation.getScrollStat().setPresentationId(pi.getPresentationId());
                }
                init();
            }

            @Override
            public void onPresentationSelectPageClose() {
                chooseFl.setVisibility(GONE);
            }
        });

        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.choose_fl, chooseFragment);
        ft.commit();
    }

    public void init(){
        initDatas();
        initViews();

    }

    public void reload(){
        drawView.clear();

        presentation.setRoomId(presentationAdapter.getRoomId());

        try {
            presentation.initPresentation(displayWidth, screenHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }

        presentation.setTotalWidth(displayWidth);
        presentation.loadPresentation(getContext(), imageUrl, new OnPresentationLoaded());

    }

    private void initDatas(){
        dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

//        presentation = new Presentation(presentationAdapter.getPresentationName());
//        presentationWeakReference = new WeakReference<>(presentation);

        presentation.setPresentationFrame(docll);
        selfWeakReference = new WeakReference<>(this);

//        presentation.setPresentationId("1");
//        presentation.setRoomId(presentationAdapter.getRoomId());

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

            whiteBoard.pageNumTv.setText(presentation.getCurrentPage() + "/" + presentation.getPresentationCount());
            whiteBoard.scrollView.setOnScrollListener(new HgScrollView.OnScrollListener() {
                @Override
                public void onScrollChanged(int top, int oldt) {
                    if(whiteBoard.seek)return;
                    whiteBoard.scrolling=true;
                    int curPage = presentation.computeCurrentPage(top, oldt);
                    presentation.setCurrentPage(curPage);

                    whiteBoard.scrollSeekBar.setProgress((int)(1000*top/(float)presentation.getTotalHeight()));
                    //TODO display currentPage
                    whiteBoard.pageNumTv.setText(curPage + "/" + presentation.getPresentationCount());


//                    ToastSingle.showCenterToast("top: " + top + "|oldt: " + oldt, Toast.LENGTH_SHORT);
//                    ToastSingle.showCenterToast("current page: " + curPage, Toast.LENGTH_SHORT);

                    presentation.sendScroll(top);

                    whiteBoard.scrolling=false;
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


            menuBarController = new MenuBarController(whiteBoard.getContext(), whiteBoard.menull);
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

                @Override
                public void onEnableScroll() {
                    whiteBoard.scrollView.clearExcludedRects();
                    drawView.setDrawable(false);
                }

                @Override
                public void onDisableScroll() {
                    whiteBoard.scrollView.addExcludedRectFs(whiteBoard.displayRect);
                    drawView.setDrawable(true);
                }
            });

            menuBarController.setColorPanel(whiteBoard.colorPanel);
            menuBarController.setWidthPanel(whiteBoard.widthPanel);
            menuBarController.setShapePanel(whiteBoard.shapePanel);
            menuBarController.setColorPointer(whiteBoard.colorPointer);

            menuBarController.init();

            TopBarController topBarController = new TopBarController(whiteBoard.topBar);
            topBarController.setPageNumTv(whiteBoard.pageNumTv);
            topBarController.setProgressPanel(whiteBoard.progressPanel);
            topBarController.setScrollSeekBar(whiteBoard.scrollSeekBar);
            topBarController.setOnSeek(new TopBarController.OnSeek() {
                @Override
                public void onSeek(float posRatio) {

                    if(whiteBoard.scrolling)return;
                    whiteBoard.seek = true;
                    int curPage = setCurrentPage(posRatio);

                    int top = presentation.getPagePositions().get(curPage-1);
                    scrollToDst(top);

                    whiteBoard.seek=false;
                }

            });
            topBarController.init();

            whiteBoard.closeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    whiteBoard.setVisibility(GONE);
                    //TODO close view
                    presentation.sendClose();
                }
            });


        }

        public void endSession(){
            if(presentation!=null){
                presentation.sendEnd();
            }
        }


        private int setCurrentPage(float posRatio) {
            int curPage = calculateCurrentPage(posRatio);
            whiteBoard.pageNumTv.setText(curPage + "/" + presentation.getPresentationCount());
            presentation.setCurrentPage(curPage);
            return curPage;
        }

        private int calculateCurrentPage(float posRatio) {
            int curPage = (int)((posRatio*(presentation.getPresentationCount()-1)))+1;

            curPage=curPage<0?0:curPage;
            return curPage;
        }

        private void scrollToDst(int top) {
            whiteBoard.scrollView.scrollTo(0, top);
            presentation.sendScroll(top);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Integer integer) {
        }

        @Override
        public void onWhiteBoard(int height) {
            drawView.setHeight(height);
        }
    }

    private void initViews(){


        docll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(startUp){
                    startUp=false;
                    setExcludedRect();
                }
            }
        });

        //init controller
        drawView = new DrawViewController(drawLayout);
        drawViewWeakReference = new WeakReference<>(drawView);

        if(!startUp){
            setExcludedRect();
            menuBarController.clearActives(null);
        }
        drawView.setDrawable(true);


        scrollViewWeakReference = new WeakReference<>(scrollView);
        displayWidth = screenWidth - ViewHelpers.dp2px(71, getContext());
        presentation.setTotalWidth(displayWidth);

        try {
            presentation.initPresentation(displayWidth, screenHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }

        presentation.loadPresentation(getContext(), imageUrl, new OnPresentationLoaded());
    }

    private void setExcludedRect() {
        int docWidth = docll.getWidth();
        displayRect = new RectF(0, 0, docWidth, screenHeight);
        scrollView.addExcludedRectFs(displayRect);
    }


    /**
     * interfaces
     */

//    public void loadPresentation(String imageUrl, int count, String roomId, String presentationId){
//        presentation.reload(getContext(), imageUrl, count, roomId, presentationId);
//    }

    private void findViews(){
        scrollView = (HgScrollView)findViewById(R.id.sv);
        drawLayout = (DrawLayout)findViewById(R.id.draw_sender_view);
        docll = (LinearLayout)findViewById(R.id.doc_ll);
        menull = (LinearLayout) findViewById(R.id.menu_frame);
        colorPanel = (ColorPopoutMenu) findViewById(R.id.color_panel);
        widthPanel = (WidthPanel) findViewById(R.id.width_panel);
        shapePanel = (ShapePanel)findViewById(R.id.shape_panel);
        progressPanel = (PopoutMenu)findViewById(R.id.progress_bar);
        pageNumTv = (TextView)findViewById(R.id.page_number_tv);
        topBar = (RelativeLayout)findViewById(R.id.top_bar_rl);
        colorPointer = (ColorPointer)findViewById(R.id.color_pointer);
        scrollSeekBar = (SeekBar)findViewById(R.id.page_scroll_seekbar);
        closeBtn = (ImageView)findViewById(R.id.btn_close_wb);
        chooseFl = (FrameLayout)findViewById(R.id.choose_fl);
        choosePresentationBtn = (ImageView)findViewById(R.id.top_folder_iv);

        choosePresentationBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initChoosePresentationFragment(true);
            }
        });
//        pageEt = (EditText)findViewById(R.id.page_to_go);
//        pageBtn = (TextView)findViewById(R.id.page_btn);
    }

}
