package com.hgxx.whiteboard.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.views.HgWhiteBoard;
import com.hgxx.whiteboard.views.HgWhiteBoardRcv;

/**
 * Created by ly on 04/05/2017.
 */

public class TestActivity extends Activity implements View.OnClickListener{


    private HgWhiteBoard hgWhiteBoard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        hgWhiteBoard = (HgWhiteBoard) findViewById(R.id.wb);
        hgWhiteBoard.setImageUrl("https://tvl.hongguaninfo.com:443/Test/api_");
        hgWhiteBoard.init();




        /*change presentation*/
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                hgWhiteBoard.setImageUrl("http://10.0.0.177:8500/Test/api_");
//                hgWhiteBoard.reload();
//
//            }
//        }, 10000);

//        Presentation ps = new Presentation();
//        ps.setPresentationName("test");




//        Observable<Integer> ob = ps.getPresentationImages(null);
//        ob.subscribe(new Observer<Integer>() {
//            @Override
//            public void onCompleted() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(WhiteBoardApplication.getContext(), "complete", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onNext(Integer integer) {
//
//            }
//        });
//
//        System.out.println("fuck");

    }


    @Override
    public void onClick(View v) {
        hgWhiteBoard.setVisibility(View.VISIBLE);
    }
}
