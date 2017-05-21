package com.hgxx.whiteboard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.hgxx.whiteboard.R;

import java.util.Arrays;


/**
 * Created by ly on 20/05/2017.
 */

public class PopoutMenu extends RelativeLayout {

    protected int direction;
    protected int anchorId;
    protected int viewWidth;
    protected int viewHeight;
    protected float cornerRadius;
    private int elevation;
    protected Paint mPaint;
    private float viewBottom;
    private float viewLeft;
    private float viewTop;
    private float viewRight;
    private int shadowColor = Color.parseColor("#999999");
    private int bgColor = Color.parseColor("#ffffff");
    private float shadowOffset;
    private Paint shadowPaint;
    private float shadowPaddingLeftTop;
    private float shadowPaddingRightBottom;
    private int actHeight;
    private int actWidth;
    private View parent;
    private int anchorChild;
    private View anchorView;
    private View anchorChildView;

    private boolean hide = true;
    private int animationDuration = 309;


    public PopoutMenu(Context context) {
        this(context, null);
    }

    public PopoutMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopoutMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttr(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initListener(View v){
        if(v!=null){
            v.setClickable(true);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle();
                }
            });
//            v.setFocusableInTouchMode(true);
        }

//        this.setFocusableInTouchMode(true);
//        getRootView().setClickable(true);
//        getRootView().setFocusableInTouchMode(true);
//        this.setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus&&!hide&&(v==null?true:!v.hasFocus())){
//                    hide();
//                }
//            }
//        });
    }

    public synchronized void toggle(){
        if(hide){
            emit();
        }
        else{
            hide();
        }

    }

    public void emit(){
        hide=false;
        this.animate().translationX(viewWidth).setDuration(animationDuration).start();
    }

    public void hide(){
        hide=true;
        this.animate().translationX(-viewWidth).setDuration(animationDuration).start();
    }


    private void initPaint(){
        mPaint = new Paint();
        mPaint.setColor(bgColor);
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        shadowPaint = new Paint();
        shadowPaint.setColor(shadowColor);
        shadowPaint.setAntiAlias(true);
        shadowPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }


    private void getAttr(Context context, AttributeSet attributeSet, int defStyleAtt){
        TypedArray att = context.obtainStyledAttributes(attributeSet, R.styleable.PopoutMenu, defStyleAtt, 0);
        direction = att.getInt(R.styleable.PopoutMenu_animationDirection,4);
        anchorId = att.getResourceId(R.styleable.PopoutMenu_anchor, 0);
        cornerRadius = (int)att.getDimension(R.styleable.PopoutMenu_menuCornerRadius, 0);
        elevation = (int)att.getDimension(R.styleable.PopoutMenu_menuElevation, 0);
        anchorChild = att.getResourceId(R.styleable.PopoutMenu_anchorChild, 0);

        shadowOffset = 0.3f*elevation;
        shadowPaddingLeftTop = elevation-shadowOffset;
        shadowPaddingRightBottom = elevation+shadowOffset;

        int[] sysAtt = new int[]{
                android.R.attr.layout_width,
                android.R.attr.layout_height
        };

        Arrays.sort(sysAtt);

        TypedArray sysAttr = context.obtainStyledAttributes(attributeSet, sysAtt, defStyleAtt, 0);
        actWidth = (int)sysAttr.getDimension(0, 0);
        actHeight = (int)sysAttr.getDimension(1, 0);



    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Path path = new Path();
        path.moveTo(viewLeft, viewTop);
        float roundStart = viewRight - cornerRadius;
        path.lineTo(roundStart, viewTop);


        float rectLeft = viewRight - 2 * cornerRadius;
        RectF roundRectf = new RectF(rectLeft, viewTop, viewRight, viewBottom);
        path.arcTo(roundRectf, -90, 180);

        path.lineTo(viewLeft, viewBottom);
        path.lineTo(viewLeft, viewTop);

        mPaint.setStyle(Paint.Style.FILL);
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint);
        mPaint.setShadowLayer(elevation, shadowOffset, shadowOffset, shadowColor);

        canvas.drawPath(path, mPaint);


        super.dispatchDraw(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


        int h = actHeight + 2*elevation;
        int w = actWidth + 2*elevation;
        int widthSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);

        super.onMeasure(widthSpec, heightSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();

        viewLeft = shadowPaddingLeftTop;
        viewBottom = viewHeight - shadowPaddingRightBottom;
        viewTop = shadowPaddingLeftTop;
        viewRight = viewWidth - shadowPaddingRightBottom;

        cornerRadius = cornerRadius==0?actHeight/2f:cornerRadius;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int selfLeft=0;
        int selfTop=0;
        float dstLeft=0;
        float dstTop=0;
        if(anchorId!=0&&anchorChild!=0){
            parent = (View)getParent();
            anchorView = parent.findViewById(anchorId);
            anchorChildView = parent.findViewById(anchorChild);
            dstLeft = anchorView.getRight()-viewWidth;
            dstTop = anchorChildView.getTop() + anchorView.getTop();
            initListener(anchorChildView);
        }
        else if(anchorId!=0){
            parent = (View)getParent();
            anchorView = parent.findViewById(anchorId);
            dstLeft = anchorView.getRight()-viewWidth;
            dstTop = anchorView.getTop();
            initListener(anchorView);
        }
        else{
            dstLeft = left+shadowPaddingLeftTop;
            dstTop = top+shadowPaddingLeftTop;
        }

        selfLeft = (int)(dstLeft-shadowPaddingLeftTop);
        selfTop = (int)(dstTop-shadowPaddingLeftTop);

        if(selfLeft!=left||selfTop!=top){
            layout(selfLeft, selfTop, selfLeft+viewWidth, selfTop+viewHeight);
        }

        if(anchorView!=null){
            anchorView.bringToFront();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if(action==MotionEvent.ACTION_DOWN){
            requestFocus();
        }


        return super.onTouchEvent(event);
    }

    public RectF getContentRect(){
        return  new RectF(shadowPaddingLeftTop, shadowPaddingLeftTop, actWidth+shadowPaddingLeftTop, actHeight+shadowPaddingLeftTop);
    }

    public int getActHeight() {
        return actHeight;
    }

    public int getActWidth() {
        return actWidth;
    }
}
