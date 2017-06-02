package com.hgxx.whiteboard.views.menu;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.LinkAddress;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.utils.ToastSingle;
import com.hgxx.whiteboard.views.ColorPointer;
import com.hgxx.whiteboard.views.ColorPopoutMenu;
import com.hgxx.whiteboard.views.PopoutMenu;
import com.hgxx.whiteboard.views.WidthPanel;
import com.hgxx.whiteboard.views.drawview.DrawControl;
import com.hgxx.whiteboard.views.drawview.DrawLayout;

/**
 * Created by ly on 12/05/2017.
 */

public class MenuBarController{

    LinearLayout menuBar;
    DrawControl drawControl;
    ColorPopoutMenu colorPanel;
    WidthPanel widthPanel;
    ShapePanel shapePanel;
    ColorPointer colorPointer;
    RectF displayRect;

    Context mContext;

    boolean scrollEnable = false;

    public boolean isScrollEnable() {
        return scrollEnable;
    }

    public void setScrollEnable(boolean scrollEnable) {
        this.scrollEnable = scrollEnable;
    }

    public MenuBarController(Context context, LinearLayout menuBar){
        this.menuBar = menuBar;
        mContext = context;
    }

    public DrawControl getDrawControl() {
        return drawControl;
    }

    public void setDrawControl(DrawControl drawControl) {
        this.drawControl = drawControl;
    }

    public void init(){
        initButons();
        initColorPanel();
        initWidthPanel();
        initShapePanel();
    }

    private void initShapePanel(){
        if(shapePanel!=null) {
            shapePanel.setOnShapeSelected(new ShapePanel.OnShapeSelected() {
                @Override
                public void onShapeSeleted(String shape) {
                    drawControl.setDrawType(shape);
                }
            });
        }
    }


    private void initWidthPanel(){

        if(widthPanel!=null){
            widthPanel.setOnWidthSelected(new WidthPanel.OnWidthSelected() {
                @Override
                public void onWidthSelected(float ratio) {
                    float strokeWidth = calculateStrokeWidth(ratio);
                    drawControl.setStrokeWidth(strokeWidth);
                }
            });
        }

    }

    private float calculateStrokeWidth(float ratio){
        return mContext.getResources().getDisplayMetrics().heightPixels*ratio;
    }


    private void initColorPanel(){
        if(colorPanel==null)return;
        colorPanel.setOnColorMoveListener(new ColorPopoutMenu.OnColorMoveListener() {
            @Override
            public void onColorMove(float cpPosX, String color) {
                showColorPointer();
                drawControl.setPaintColor(color);
            }
        });

//        if(colorPanel!=null){
//            colorPanel.setOnColorSelected(new ColorPanel.OnColorSelected() {
//                @Override
//                public void onColorSelected(String color) {
//                    drawControl.setPaintColor(color);
//                    colorPanel.setVisibility(View.GONE);
//                }
//            });
//        }
    }



    public interface OnMenuBtnClick {
        void onClear();
        void onUndo();
        void onEnableScroll();
        void onDisableScroll();
    }
    private OnMenuBtnClick onBtnClick;

    public void setOnBtnClick(OnMenuBtnClick onBtnClick) {
        this.onBtnClick = onBtnClick;
    }


    private void setBtnActive(View v){
        if(v.getId()!=R.id.hand_btn){
            if(onBtnClick!=null){
                onBtnClick.onDisableScroll();
            }
        }
        v.setBackgroundColor(Color.parseColor("#e4e4e4"));
        clearActives(v);

    }

    public void clearActives(View v){
        for(int i=2;i<menuBar.getChildCount();i++){
            LinearLayout btn = (LinearLayout)menuBar.getChildAt(i);
            if(v==null||btn!=v){
                btn.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }
    }

    private void initButons(){
        View.OnClickListener onMenuClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int viewId = v.getId();
//                if(viewId==R.id.presentation_btn) {
//                    ToastSingle.showCenterToast("测试版只提供一个课件...", Toast.LENGTH_SHORT);
                /*}else*/
                if(viewId==R.id.hand_btn){
                    toggleScroll(v);
                    clearAllPanel();
                }else if (viewId==R.id.clear_btn){
                    drawControl.clear();
                    if(onBtnClick!=null){
                        onBtnClick.onClear();
                    }
                }else if(viewId==R.id.color_btn){
                    disableScroll();
                    setBtnActive(v);
                    if(colorPanel!=null){
                        togglePanel(colorPanel);
                    }
                    int left = colorPointer.getLeft();
                    int top = colorPointer.getTop();
                    System.out.println("fuck");


                }
                else if(viewId==R.id.draw_type_btn) {
//                    drawControl.setDrawable(true);
                    disableScroll();
                    setBtnActive(v);
                    if(shapePanel!=null){
                        togglePanel(shapePanel);
                    }
                }else if(viewId==R.id.stroke_width_btn) {
                    disableScroll();
                    setBtnActive(v);
                    if(widthPanel!=null){
                        togglePanel(widthPanel);
                    }
                }else if(viewId==R.id.undo_btn) {
                    drawControl.undo();
                    if(onBtnClick!=null){
                        onBtnClick.onUndo();
                    }
                }


            }
        };

        for(int i=0;i< menuBar.getChildCount();i++){
            LinearLayout btn = (LinearLayout)menuBar.getChildAt(i);
//            TextView btn = (TextView)ml.getChildAt(0);
            btn.setOnClickListener(onMenuClickListener);
        }

    }

    public void toggleScroll(){
        toggleScroll(menuBar.findViewById(R.id.hand_btn));
    }

    public void toggleScroll(View v){
        if(onBtnClick==null)return;
        if(isScrollEnable()){
            clearActive(v);
            disableScroll();
        }
        else{
            setBtnActive(v);
            enableScroll();
        }
    }

    private void enableScroll() {
        setScrollEnable(true);
        onBtnClick.onEnableScroll();
    }

    private void disableScroll() {
        setScrollEnable(false);
        onBtnClick.onDisableScroll();
    }

    private void clearActive(View v){
        v.setBackgroundColor(Color.parseColor("#ffffff"));
    }


    public synchronized void togglePanel(PopoutMenu v){
        if(v.isHide()){
            clearAllPanel();
            if(v.getId()==R.id.color_panel){
                v.emit(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showColorPointer();
                        int left = colorPointer.getLeft();
                        int top = colorPointer.getTop();
                        System.out.println("fuck");
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
                v.emit();
            }
        }
        else{

            if(v.getId()==R.id.color_panel){
                hideColorPointer();
            }
            v.hide();
        }
    }

    private void hideColorPointer(){
        colorPointer.setVisibility(View.GONE);
    }

    private void showColorPointer(){
        int cpLeft = (int)(colorPanel.getX()+colorPanel.getCpPosX()-colorPointer.getWidth()/2f);
        int cpTop = (int)(colorPanel.getTop()+colorPanel.getCpPosY()-colorPointer.getHeight());

        int wid = colorPointer.getWidth();

        colorPointer.layout(cpLeft, cpTop, cpLeft + colorPointer.getWidth(), cpTop + colorPointer.getHeight());
        colorPointer.setColor(colorPanel.getCpColor());
        colorPointer.setVisibility(View.VISIBLE);
    }


    public void clearAllPanel(){
        colorPanel.hide();
        shapePanel.hide();
        widthPanel.hide();
        colorPointer.setVisibility(View.GONE);
    }


    public ColorPopoutMenu getColorPanel() {
        return colorPanel;
    }

    public void setColorPanel(ColorPopoutMenu colorPanel) {
        this.colorPanel = colorPanel;
        initColorPanel();
    }

    public WidthPanel getWidthPanel() {
        return widthPanel;
    }

    public void setWidthPanel(WidthPanel widthPanel) {
        this.widthPanel = widthPanel;
    }

    public ShapePanel getShapePanel() {
        return shapePanel;
    }

    public void setShapePanel(ShapePanel shapePanel) {
        this.shapePanel = shapePanel;
    }

    public ColorPointer getColorPointer() {
        return colorPointer;
    }

    public void setColorPointer(ColorPointer colorPointer) {
        this.colorPointer = colorPointer;
    }

    public void setDisplayRect(RectF displayRect) {
        this.displayRect = displayRect;
    }
}
