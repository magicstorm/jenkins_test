package com.hgxx.whiteboard.ui;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.WhiteBoardApplication;
import com.hgxx.whiteboard.models.Presentation;
import com.hgxx.whiteboard.utils.ToastSingle;
import com.hgxx.whiteboard.utils.ViewHelpers;
import com.hgxx.whiteboard.views.HgScrollView;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;
import com.hgxx.whiteboard.views.menu.ColorPanel;
import com.hgxx.whiteboard.views.menu.MenuBarController;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.WeakReference;

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
    private Presentation presentation;

    private static WeakReference<DrawViewController> drawViewWeakReference;
    private static WeakReference<Presentation> presentationWeakReference;
    private static WeakReference<HgScrollView> scrollViewWeakReference;
    private static WeakReference<WhiteBoardActivity> selfActivityWeakReference;
    private LinearLayout menull;
    private ColorPanel colorPanel;
    private LinearLayout widthPanel;
    private LinearLayout shapePanel;
    private TextView pageNumTv;
    private EditText pageEt;
    private TextView pageBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board);
        findViews();
        initDatas();
        initViews();


        initSocketClient();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        presentation.onDestroy(this);
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

        presentation = new Presentation("Test");
        presentationWeakReference = new WeakReference<>(presentation);
        presentation.setPresentationFrame(docll);
        selfActivityWeakReference = new WeakReference<>(this);

    }

    static class OnPresentationLoaded implements Presentation.OnLoadPresentationCallBack{

        private DrawViewController drawView;
        private Presentation presentation;
        private WhiteBoardActivity selfActivity;

        public OnPresentationLoaded(){
            this.drawView = drawViewWeakReference.get();
            this.presentation = presentationWeakReference.get();
            this.selfActivity = selfActivityWeakReference.get();
        }

        @Override
        public void onLoadPresentationCompleted() {

            if(drawView==null||presentation==null||selfActivity==null)return;
            try {
                presentation.initServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            presentation.initDrawMessage(drawView);

            selfActivity.pageNumTv.setText(presentation.getCurrentPage() + "/" + presentation.getPresentationCount());
            selfActivity.scrollView.setOnScrollListener(new HgScrollView.OnScrollListener() {
                @Override
                public void onScrollChanged(int top, int oldt) {
                    int curPage = presentation.computeCurrentPage(top, oldt);
                    presentation.setCurrentPage(curPage);

                    //TODO display currentPage
                    selfActivity.pageNumTv.setText(curPage + "/" + presentation.getPresentationCount());

//                    ToastSingle.showCenterToast("current page: " + curPage, Toast.LENGTH_SHORT);

                    presentation.sendScroll(top);
                }
            });

            selfActivity.pageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pageNum = selfActivity.pageEt.getText().toString();
                    int no = Integer.parseInt(pageNum);
                    if(no<=presentation.getPresentationCount()&&no>=1){
                        int curTop = presentation.getPagePositions().get(no-1);
                        selfActivity.scrollView.scrollTo(0, curTop);
                        presentation.sendScroll(curTop);
                    }
                }
            });


            MenuBarController menuBarController = new MenuBarController(WhiteBoardApplication.getContext(), selfActivity.menull);
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

            menuBarController.setColorPanel(selfActivity.colorPanel);
            menuBarController.setWidthPanel(selfActivity.widthPanel);
            menuBarController.setShapePanel(selfActivity.shapePanel);;


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
        int displayWidth = screenWidth - ViewHelpers.dp2px(101*2, this);
        presentation.setTotalWidth(displayWidth);

        try {
            presentation.initPresentation(displayWidth, screenHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }

        presentation.loadPresentation(this, new OnPresentationLoaded());

    }







    private void findViews(){
        scrollView = (HgScrollView)findViewById(R.id.sv);
        drawLayout = (DrawLayout)findViewById(R.id.draw_sender_view);
        docll = (LinearLayout)findViewById(R.id.doc_ll);
        menull = (LinearLayout)findViewById(R.id.menu);
        colorPanel = (ColorPanel)findViewById(R.id.color_panel);
        widthPanel = (LinearLayout)findViewById(R.id.width_panel);
        shapePanel = (LinearLayout)findViewById(R.id.shape_panel);
        pageNumTv = (TextView)findViewById(R.id.page_number_tv);
        pageEt = (EditText)findViewById(R.id.page_to_go);
        pageBtn = (TextView)findViewById(R.id.page_btn);
    }


}
