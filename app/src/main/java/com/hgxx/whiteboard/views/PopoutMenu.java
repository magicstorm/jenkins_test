package com.hgxx.whiteboard.views;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.hgxx.whiteboard.R;

import java.lang.reflect.Field;
import java.util.Arrays;


/**
 * Created by ly on 20/05/2017.
 */

public class PopoutMenu extends RelativeLayout {


    private static final int DIRECTION_LEFT = 1;
    private static final int DIRECTION_RIGHT = 2;
    private static final int DIRECTION_UP = 3;
    private static final int DIRECTION_DOWN = 4;


    private static final int ANCHOR_DIRECTION_HORIZONTAL = 2;
    private static final int ANCHOR_DIRECTION_VERTICAL = 1;

    private boolean adjustSize = false;
    protected int direction;
    protected int anchorId;
    protected int viewWidth;
    protected int viewHeight;
    protected float cornerRadius;
    private int elevation;
    protected Paint mPaint;
    protected float viewBottom;
    protected float viewLeft;
    protected float viewTop;
    protected float viewRight;
    private int shadowColor = Color.parseColor("#999999");
    private int bgColor = Color.parseColor("#ffffff");
    private float shadowOffset;
    private Paint shadowPaint;
    private float minShadow;
    private float maxShadow;
    private int actHeight;
    private int actWidth;
    private View parent;
    private int anchorChild;
    private View anchorView;
    private View anchorChildView;

    private boolean hide = true;
    private int animationDuration = 309;

    private int animateDistance;
    private int anchorDirection;

    private boolean stayTop = false;
    private boolean addAnchorListener;

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
        if(!addAnchorListener)return;
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

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
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
        emit(null);
    }

    public void emit(Animator.AnimatorListener animatorListener){
        hide=false;
        if(direction==DIRECTION_RIGHT){
            this.animate().translationX(animateDistance).setDuration(animationDuration).setListener(animatorListener).start();
        }else if(direction==DIRECTION_LEFT){
            this.animate().translationX(-animateDistance).setDuration(animationDuration).setListener(animatorListener).start();
        }
    }

    public void hide(){
        hide(null);
    }
    public void hide(Animator.AnimatorListener animatorListener){
        hide=true;
        if(direction==DIRECTION_RIGHT){
            this.animate().translationX(0).setDuration(animationDuration).setListener(animatorListener).start();
        }else if(direction==DIRECTION_LEFT){
            this.animate().translationX(0).setDuration(animationDuration).setListener(animatorListener).start();
        }
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
        direction = att.getInt(R.styleable.PopoutMenu_animationDirection,2);
        anchorId = att.getResourceId(R.styleable.PopoutMenu_anchor, 0);
        cornerRadius = (int)att.getDimension(R.styleable.PopoutMenu_menuCornerRadius, 0);
        elevation = (int)att.getDimension(R.styleable.PopoutMenu_menuElevation, 0);
        anchorChild = att.getResourceId(R.styleable.PopoutMenu_anchorChild, 0);
        animateDistance = (int)att.getDimension(R.styleable.PopoutMenu_animationDistance, 0);
        anchorDirection = att.getInt(R.styleable.PopoutMenu_anchorDirectioon, 1);
        stayTop = att.getBoolean(R.styleable.PopoutMenu_stayTop, false);
        addAnchorListener = att.getBoolean(R.styleable.PopoutMenu_addAnchorListener, true);
        adjustSize = att.getBoolean(R.styleable.PopoutMenu_adjustSize, false);



        shadowOffset = 0.3f*elevation;

        minShadow = elevation-shadowOffset;
        maxShadow = elevation+shadowOffset;

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

        if(direction==DIRECTION_RIGHT){
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
        }
        else if(direction==DIRECTION_LEFT){
            path.moveTo(viewRight, viewTop);
            float roundStart = viewLeft + cornerRadius;
            path.lineTo(roundStart, viewTop);

            float rectRight = viewLeft + 2 * cornerRadius;
            RectF roundRectf = new RectF(viewLeft, viewTop, rectRight, viewBottom);
            path.arcTo(roundRectf, -90, -180);

            path.lineTo(viewRight, viewBottom);
            path.lineTo(viewRight, viewTop);


            mPaint.setStyle(Paint.Style.FILL);
            setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint);
            mPaint.setShadowLayer(elevation, -shadowOffset, shadowOffset, shadowColor);
        }


        canvas.drawPath(path, mPaint);


        super.dispatchDraw(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        parent = (View)getParent();
        anchorView = parent.findViewById(anchorId);
        anchorChildView = parent.findViewById(anchorChild);

        if(anchorDirection==ANCHOR_DIRECTION_VERTICAL&&adjustSize){
            if(anchorChildView!=null){
                actHeight = anchorChildView.getMeasuredHeight();
            }
            else if(anchorView!=null){
                actHeight = anchorView.getMeasuredHeight();
            }
        }

//        if(anchorChildView!=null){
//            anchorChildView.getMeasuredHeight()
//        }


        int h = actHeight + 2*elevation;
        int w = actWidth + 2*elevation;
        int widthSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);

        super.onMeasure(widthSpec, heightSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();

        animateDistance = animateDistance==0?viewWidth:animateDistance;

        if(direction==DIRECTION_RIGHT){
            viewLeft = minShadow;
            viewBottom = viewHeight - maxShadow;
            viewTop = minShadow;
            viewRight = viewWidth - maxShadow;
        }
        else if(direction==DIRECTION_LEFT){
            viewLeft = maxShadow;
            viewBottom = viewHeight - maxShadow;
            viewTop = minShadow;
            viewRight = viewWidth - minShadow;
        }

        cornerRadius = cornerRadius==0?actHeight/2f:cornerRadius;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int selfLeft=0;
        int selfTop=0;
        float dstLeft=0;
        float dstTop=0;
        if(anchorId!=0&&anchorChild!=0){
//            parent = (View)getParent();
//            anchorView = parent.findViewById(anchorId);
//            anchorChildView = parent.findViewById(anchorChild);

            if(direction==DIRECTION_RIGHT){
                dstLeft = anchorView.getLeft()+anchorChildView.getRight()-viewWidth;
            }
            else if(direction==DIRECTION_LEFT){
                dstLeft = anchorView.getLeft() + anchorChildView.getLeft();
            }

            dstTop = anchorChildView.getTop() + anchorView.getTop();
            initListener(anchorChildView);
        }
        else if(anchorId!=0){
//            parent = (View)getParent();
//            anchorView = parent.findViewById(anchorId);

            if(direction==DIRECTION_RIGHT){
                dstLeft = anchorView.getRight()-viewWidth;
            }
            else if(direction==DIRECTION_LEFT){
                dstLeft = anchorView.getLeft();
            }

            dstTop = anchorView.getTop();
            initListener(anchorView);
        }
        else{
            if(direction==DIRECTION_RIGHT){
                dstLeft = left + minShadow;
            }
            else if(direction==DIRECTION_LEFT){
                dstLeft = left + maxShadow;
            }

            dstTop = top+ minShadow;
        }

        if(direction==DIRECTION_RIGHT){
            selfLeft = (int)(dstLeft- minShadow);
        }
        else if(direction==DIRECTION_LEFT){
            selfLeft = (int)(dstLeft+ maxShadow);
        }

        selfTop = (int)(dstTop- minShadow);
        if(selfLeft!=left||selfTop!=top){
            layout(selfLeft, selfTop, selfLeft+viewWidth, selfTop+viewHeight);
        }


        if(direction==DIRECTION_LEFT){
            adjustChildLayout(maxShadow, minShadow);
        }else if(direction==DIRECTION_RIGHT){
            adjustChildLayout(minShadow, minShadow);
        }


        if(anchorView!=null&&!stayTop){
            anchorView.bringToFront();
        }

    }

    private void adjustChildLayout(float leftShadow, float topShadow) {
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutParams st =
                        (LayoutParams) child.getLayoutParams();

                try {
                    Field fLeft = LayoutParams.class.getDeclaredField("mLeft");
                    Field fRight = LayoutParams.class.getDeclaredField("mRight");
                    Field fTop = LayoutParams.class.getDeclaredField("mTop");
                    Field fBottom = LayoutParams.class.getDeclaredField("mBottom");
                    fLeft.setAccessible(true);
                    fRight.setAccessible(true);
                    fTop.setAccessible(true);
                    fBottom.setAccessible(true);


                    child.layout((int)((int)fLeft.get(st)-leftShadow/2), (int)((int)fTop.get(st)-topShadow/2), (int)((int)fRight.get(st)-leftShadow/2), (int)((int)fBottom.get(st)-topShadow/2));

                } catch (NoSuchFieldException e) {

                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }


            }
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
        return  new RectF(minShadow, minShadow, actWidth+ minShadow, actHeight+ minShadow);
    }

    public int getActHeight() {
        return actHeight;
    }

    public int getActWidth() {
        return actWidth;
    }

    public int getAnimateDistance() {
        return animateDistance;
    }

    public void setAnimateDistance(int animateDistance) {
        this.animateDistance = animateDistance;
    }

    public int getAnchorDirection() {
        return anchorDirection;
    }

    public void setAnchorDirection(int anchorDirection) {
        this.anchorDirection = anchorDirection;
    }

    public boolean isAdjustSize() {
        return adjustSize;
    }

    public void setAdjustSize(boolean adjustSize) {
        this.adjustSize = adjustSize;
    }
}
