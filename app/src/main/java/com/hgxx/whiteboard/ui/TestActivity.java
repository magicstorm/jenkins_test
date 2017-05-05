package com.hgxx.whiteboard.ui;

import android.app.Activity;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.WhiteBoardApplication;
import com.hgxx.whiteboard.entities.Presentation;

import rx.Observable;
import rx.Observer;

/**
 * Created by ly on 04/05/2017.
 */

public class TestActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        Presentation ps = new Presentation();
        ps.setPresentationName("test");
        Observable<Integer> ob = ps.getPresentationImages(null);
        ob.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WhiteBoardApplication.getContext(), "complete", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {

            }
        });

        System.out.println("fuck");

    }
}
