package com.hgxx.whiteboard.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.hgxx.whiteboard.R;

/**
 * Created by ly on 27/04/2017.
 */

public class HomePageActivity extends AppCompatActivity{

    private RadioButton rcvBtn;
    private RadioButton senderBtn;
    private Button submitBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        findViews();
        initListeners();
    }

    private void findViews(){
        rcvBtn = (RadioButton)findViewById(R.id.receiver_rd);
        senderBtn = (RadioButton)findViewById(R.id.sender_rd);
        submitBtn = (Button)findViewById(R.id.submit);
    }

    private void initListeners(){
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if(rcvBtn.isChecked()){
//                    intent = new Intent(HomePageActivity.this, WhiteBoardRcvActivity.class);
                }
                else{
//                    intent = new Intent(HomePageActivity.this, WhiteBoardActivity.class);
                }

                HomePageActivity.this.startActivity(intent);
            }
        });
    }
}
