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
import com.hgxx.whiteboard.models.PresentationInfo;
import com.hgxx.whiteboard.views.HgWhiteBoard;
import com.hgxx.whiteboard.views.HgWhiteBoardRcv;
import com.hgxx.whiteboard.views.PresentationAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by ly on 04/05/2017.
 */

public class TestActivity extends Activity implements View.OnClickListener{


    private HgWhiteBoard hgWhiteBoard;
    private HgWhiteBoardRcv hgWhiteBoardRcv;
    private ArrayList<PresentationInfo> pis = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        PresentationInfo testPI = new PresentationInfo("Swift Programming Language");
        testPI.setUploadTime("2017年5月5日 17:00");
        testPI.setSize("20MB");
        testPI.setUrl("https://tvl.hongguaninfo.com:443/Test/api_");
        testPI.setPresentationId("1");

        PresentationInfo testPI1 = new PresentationInfo("Swift Programming Language Local");
        testPI1.setUploadTime("2017年5月5日 17:00");
        testPI1.setSize("20MB");
        testPI1.setUrl("http://192.168.8.125:8500/Test/api_");
        testPI1.setPresentationId("1");
//
        pis.add(testPI);
        pis.add(testPI1);


        /**
         * sender
         */
//        hgWhiteBoard = (HgWhiteBoard) findViewById(R.id.wb);
//        hgWhiteBoard.setPresentationAdapter(new PresentationAdapter() {
//            @Override
//            public int getCount() {
//                return pis.size();
//            }
//
//            @Override
//            public PresentationInfo getPresentationInfo(int pos) {
//                if(pos>=pis.size())return null;
//                return pis.get(pos);
//            }
//
//            @Override
//            public PresentationInfo getPresentationInfo(String presentationId) {
//
//                for(int i=0;i<pis.size();i++){
//                    String psId = pis.get(i).getPresentationId();
//                    if(psId.equals(presentationId)){
//                        return pis.get(i);
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            public String getRoomId() {
//                return "1";
//            }
//
//            @Override
//            public String getPresentationName() {
//                return "Test";
//            }
//        });
//        hgWhiteBoard.setSesstionTitle("高中二年级的课");
        hgWhiteBoard.initPresInfo(this);


        /**
         * receiver
         */

        hgWhiteBoardRcv = (HgWhiteBoardRcv)findViewById(R.id.wb);
        hgWhiteBoardRcv.setImageUrl("https://tvl.hongguaninfo.com:443/Test/api_");

        PresentationAdapter presentationAdapter = new PresentationAdapter() {

            @Override
            public int getCount() {
                return 50;
            }

            @Override
            public PresentationInfo getPresentationInfo(int pos) {
                if(pos>=pis.size())return null;
                return pis.get(pos);
            }

            @Override
            public PresentationInfo getPresentationInfo(String presentationId) {
                for(int i=0;i<pis.size();i++){
                    String psId = pis.get(i).getPresentationId();
                    if(psId.equals(presentationId)){
                        return pis.get(i);
                    }
                }
                return null;
            }

            @Override
            public String getRoomId() {
                return "1";
            }

            @Override
            public String getPresentationName() {
                return "Test";
            }
        };
        hgWhiteBoardRcv.setPresentationAdapter(presentationAdapter);

        hgWhiteBoardRcv.init();




//        hgWhiteBoard.setImageUrl("https://tvl.hongguaninfo.com:443/Test/api_");
//        hgWhiteBoard.init();






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
        hgWhiteBoardRcv.setVisibility(View.VISIBLE);
    }
}
