package com.hgxx.whiteboard.views.menu;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.views.PopoutMenu;

/**
 * Created by ly on 23/05/2017.
 */

public class ShapePanel extends PopoutMenu {

    private ImageView oval;
    private ImageView rect;
    private ImageView line;

    public ShapePanel(Context context) {
        this(context, null);
    }

    public ShapePanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapePanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LinearLayout content = (LinearLayout) View.inflate(getContext(), R.layout.shape_panel_content, null);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(CENTER_VERTICAL);
        content.setLayoutParams(params);
        this.addView(content);

        oval = (ImageView)content.findViewById(R.id.shape_oval);
        rect = (ImageView)content.findViewById(R.id.shape_rect);
        line = (ImageView)content.findViewById(R.id.shape_line);
        initListener();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


    }

    public interface OnShapeSelected{
        void onShapSeleted(String shape);
    }
    private OnShapeSelected onShapeSelected;

    public void setOnShapeSelected(OnShapeSelected onShapeSelected) {
        this.onShapeSelected = onShapeSelected;
    }

    private void initListener(){
        View.OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                String shape=null;
                if(id==R.id.shape_line){
                    shape = "line";
                }else if(id==R.id.shape_oval){
                    shape = "oval";
                }else if(id==R.id.shape_rect){
                    shape = "rect";
                }
                activeView(v, shape);

                if(onShapeSelected!=null){
                    onShapeSelected.onShapSeleted(shape);
                }

            }
        };
        oval.setOnClickListener(clickListener);
        line.setOnClickListener(clickListener);
        rect.setOnClickListener(clickListener);

    }

    private void activeView(View v, String shape){
        if(shape==null)return;
        int idAct = getContext().getResources().getIdentifier("draw_type_"+shape+"_active" , "drawable", getContext().getPackageName());

        clearAll();

        ((ImageView)v).setImageDrawable(ContextCompat.getDrawable(getContext(), idAct));

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private void clearAll(){
        line.setImageResource(R.drawable.draw_type_line);
        oval.setImageResource(R.drawable.draw_type_oval);
        rect.setImageResource(R.drawable.draw_type_rect);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float dp2px(float dp){
        return (int)(dp*getContext().getResources().getDisplayMetrics().density+0.5f);
    }
}
