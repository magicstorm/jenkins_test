package com.hgxx.whiteboard.views.menu;

import android.animation.Animator;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.views.PopoutMenu;

/**
 * Created by ly on 22/05/2017.
 */

public class TopBarController {
    RelativeLayout topBar;
    PopoutMenu progressPanel;
    TextView pageNumTv;
    SeekBar scrollSeekBar;
    int maxProgress = 1000;

    public SeekBar getScrollSeekBar() {
        return scrollSeekBar;
    }

    public void setScrollSeekBar(SeekBar scrollSeekBar) {
        this.scrollSeekBar = scrollSeekBar;
    }

    public TopBarController(RelativeLayout topBar){
        this.topBar = topBar;
    }

    public void init(){
        initBtns();
        initSeekBar();
    }


    public interface OnSeek{
        void onSeek(float posRatio);
    }

    private OnSeek onSeek;

    public void setOnSeek(OnSeek onSeek) {
        this.onSeek = onSeek;
    }

    private void initSeekBar(){
        scrollSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int curProgress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                curProgress = progress;
                if(onSeek!=null){
                    onSeek.onSeek(calculateRatio());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            private float calculateRatio() {
                return curProgress/(float)maxProgress;
            }
        });
    }

    private void toggleProgressBar(){
        if(progressPanel.isHide()){
            progressPanel.emit(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    scrollSeekBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        else{
            scrollSeekBar.setVisibility(View.GONE);
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
