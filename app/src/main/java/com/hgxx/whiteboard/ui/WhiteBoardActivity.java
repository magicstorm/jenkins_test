package com.hgxx.whiteboard.ui;

import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.models.Presentation;
import com.hgxx.whiteboard.utils.ViewHelpers;
import com.hgxx.whiteboard.views.HgScrollView;
import com.hgxx.whiteboard.views.drawview.DrawLayout;
import com.hgxx.whiteboard.views.drawview.DrawViewController;

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
        presentation.onDestroy(this);
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
        presentationWeakReference = new WeakReference<Presentation>(presentation);
        presentation.setPresentationFrame(docll);

    }

    static class OnPresentationLoaded implements Presentation.OnLoadPresentationCallBack{

        private DrawViewController drawView;
        private Presentation presentation;
        private HgScrollView scrollView;

        public OnPresentationLoaded(){
            this.drawView = drawViewWeakReference.get();
            this.presentation = presentationWeakReference.get();
            this.scrollView = scrollViewWeakReference.get();
        }

        @Override
        public void onLoadPresentationCompleted() {

            if(drawView==null||presentation==null||scrollView==null)return;
            try {
                presentation.initServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            presentation.initDrawMessage(drawView);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    presentation.initScrollMessage(scrollView);
                }
            }, 2000);

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

        scrollViewWeakReference = new WeakReference<HgScrollView>(scrollView);
        int displayWidth = screenWidth - ViewHelpers.dp2px(101*2, this);
        presentation.setTotalWidth(displayWidth);

        try {
            presentation.initPresentation(displayWidth, screenHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }

        presentation.loadPresentation(this, new OnPresentationLoaded());

    }


//    private void addTestImages(int no, int width){
//        for(int i=0;i<no;i++) {
//            ImageView imageView = new ImageView(this);
//            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
//            imageView.setLayoutParams(ivParams);
//            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            imageView.setAdjustViewBounds(true);
//            presentation.getPresentationFrame().addView(imageView);
//            ImageUtils.getLoadImageObserve(this, presentation.getPresentationUrl(i), imageView, new ImageUtils.OnTargetReadyCallBack() {
//                @Override
//                public void onTargetReady(Object target) {
//                    System.out.println("source ready");
//                }
//            }, new ImageUtils.OnSizeReadyCallBack() {
//                @Override
//                public void onSizeReady(int width, int height) {
//                    System.out.println("size ready");
//                }
//            }, i).subscribe(new Observer<Integer>() {
//                @Override
//                public void onCompleted() {
//                }
//
//                @Override
//                public void onError(Throwable e) {
//
//                }
//
//                @Override
//                public void onNext(Integer integer) {
//
//                }
//            });
//        }
//    }

    private void findViews(){
        scrollView = (HgScrollView)findViewById(R.id.sv);
        drawLayout = (DrawLayout)findViewById(R.id.draw_sender_view);
        docll = (LinearLayout)findViewById(R.id.doc_ll);
    }


//    private boolean isJsonFieldNotNull(JSONObject jsonObject, String key) throws JSONException {
//        return jsonObject.has(key)&&!TextUtils.isEmpty(jsonObject.getString(key))&&!jsonObject.getString(key).equals("null");
//    }
}
