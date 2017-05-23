package com.hgxx.whiteboard.views.menu;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hgxx.whiteboard.views.PopoutMenu;

/**
 * Created by ly on 22/05/2017.
 */

public class TopBarController {
    RelativeLayout topBar;
    PopoutMenu progressPanel;
    TextView pageNumTv;



    public TopBarController(RelativeLayout topBar){
        this.topBar = topBar;
    }

    public void init(){
        initBtns();



    }

    private void toggleProgressBar(){
        if(progressPanel.isHide()){
            progressPanel.emit();
        }
        else{
            progressPanel.hide();
        }
    }


    private void initBtns(){
        if(progressPanel !=null&&pageNumTv!=null){
            pageNumTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleProgressBar();
                }
            });
        }
    }

    public RelativeLayout getTopBar() {
        return topBar;
    }

    public void setTopBar(RelativeLayout topBar) {
        this.topBar = topBar;
    }

    public PopoutMenu getProgressPanel() {
        return progressPanel;
    }

    public void setProgressPanel(PopoutMenu progressPanel) {
        this.progressPanel = progressPanel;
    }

    public TextView getPageNumTv() {
        return pageNumTv;
    }

    public void setPageNumTv(TextView pageNumTv) {
        this.pageNumTv = pageNumTv;
    }
}
