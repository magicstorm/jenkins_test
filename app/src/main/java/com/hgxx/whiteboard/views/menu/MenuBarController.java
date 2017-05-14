package com.hgxx.whiteboard.views.menu;

import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.entities.Display;
import com.hgxx.whiteboard.utils.ToastSingle;
import com.hgxx.whiteboard.views.drawview.DrawControl;

/**
 * Created by ly on 12/05/2017.
 */

public class MenuBarController{
    private float[] widths = new float[]{
            1,5,10,15,20,25
    };
    private float widthRef = 529;


    LinearLayout menuBar;
    DrawControl drawControl;
    ColorPanel colorPanel;
    LinearLayout widthPanel;
    LinearLayout shapePanel;

    Context mContext;



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
            for (int i = 0; i < shapePanel.getChildCount(); i++) {
                TextView shapeBtn = (TextView) shapePanel.getChildAt(i);
                shapeBtn.setClickable(true);
                shapeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = (String)v.getTag();
                        drawControl.setDrawType(tag);
                        shapePanel.setVisibility(View.GONE);
                    }
                });
            }
        }
    }


    private void initWidthPanel(){
        if(widthPanel!=null){
            for(int i=0;i<widthPanel.getChildCount();i++){
                final int j = i;
                TextView pointFrame = (TextView)widthPanel.getChildAt(i);
                pointFrame.setClickable(true);
                pointFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        float strokeWidth = calculateStrokeWidth(j);
                        drawControl.setStrokeWidth(strokeWidth);
                        widthPanel.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private float calculateStrokeWidth(int i){
        float ratio = (i==0?1:i*5)/widthRef;
        return mContext.getResources().getDisplayMetrics().heightPixels*ratio;
    }


    private void initColorPanel(){
        if(colorPanel!=null){
            colorPanel.setOnColorSelected(new ColorPanel.OnColorSelected() {
                @Override
                public void onColorSelected(String color) {
                    drawControl.setPaintColor(color);
                    colorPanel.setVisibility(View.GONE);
                }
            });
        }
    }

    public interface OnMenuBtnClick {
        void onClear();
        void onUndo();
    }
    private OnMenuBtnClick onBtnClick;

    public void setOnBtnClick(OnMenuBtnClick onBtnClick) {
        this.onBtnClick = onBtnClick;
    }

    private void initButons(){
        View.OnClickListener onMenuClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.presentation_btn:
                        ToastSingle.showCenterToast("测试版只提供一个课件...", Toast.LENGTH_SHORT);
                        break;
                    case R.id.clear_btn:
                        drawControl.clear();
                        if(onBtnClick!=null){
                            onBtnClick.onClear();
                        }
                        break;
                    case R.id.color_btn:
                        if(colorPanel!=null){
                            togglePanel(colorPanel);
                        }
                        break;
                    case R.id.draw_type_btn:
                        if(shapePanel!=null){
                            togglePanel(shapePanel);
                        }
                        break;
                    case R.id.stroke_width_btn:
                        if(widthPanel!=null){
                            togglePanel(widthPanel);
                        }
                        break;
                    case R.id.undo_btn:
                        drawControl.undo();
                        if(onBtnClick!=null){
                            onBtnClick.onUndo();
                        }
                        break;
                }

            }
        };

        for(int i=0;i< menuBar.getChildCount();i++){
            LinearLayout ml = (LinearLayout)menuBar.getChildAt(i);
            TextView btn = (TextView)ml.getChildAt(0);
            btn.setOnClickListener(onMenuClickListener);
        }

    }

    public void togglePanel(View v){
        if(v.getVisibility()!=View.VISIBLE){
            clearAllPanel();
            v.setVisibility(View.VISIBLE);
        }
        else{
            v.setVisibility(View.GONE);
        }
    }


    public void clearAllPanel(){
        colorPanel.setVisibility(View.GONE);
        widthPanel.setVisibility(View.GONE);
        shapePanel.setVisibility(View.GONE);
    }


    public ColorPanel getColorPanel() {
        return colorPanel;
    }

    public void setColorPanel(ColorPanel colorPanel) {
        this.colorPanel = colorPanel;
        initColorPanel();
    }

    public LinearLayout getWidthPanel() {
        return widthPanel;
    }

    public void setWidthPanel(LinearLayout widthPanel) {
        this.widthPanel = widthPanel;
    }

    public LinearLayout getShapePanel() {
        return shapePanel;
    }

    public void setShapePanel(LinearLayout shapePanel) {
        this.shapePanel = shapePanel;
    }
}
