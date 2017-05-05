package com.hgxx.whiteboard.views.drawview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hgxx.whiteboard.R;

/**
 * Created by ly on 27/04/2017.
 */

public class DrawView extends View{


    public static final int MIN_REFRESH_INTERVAL = 30;
    private long lastRefreshTime;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path    mPath;
    private Paint mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Paint mPaint;
    private int curWidth;
    private int curHeight;

    private boolean needCircle = true;

    public DrawView(Context c) {
        this(c, null);
        context=c;
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        if(needCircle){
            initCircle();
        }
        initPaint();
        getAttributes(context, attrs, defStyleAttr);
    }

    private void getAttributes(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.DrawView, defStyleAttr, 0);
        String paintColor = customAttrs.getString(R.styleable.DrawView_paintColor);
        int paintWidth = (int)customAttrs.getDimension(R.styleable.DrawView_paintWidth,0);
        boolean needCircle = customAttrs.getBoolean(R.styleable.DrawView_needCircle, true);
        if(!TextUtils.isEmpty(paintColor)){
            setPaintColor(paintColor);
        }
        if(paintWidth!=0){
            setStrokeWidth(paintWidth);
        }
        setNeedCircle(needCircle);
    }

    private void initCircle(){
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }

//    public interface OnClearScreen{
//        void onClearScreen();
//    }
//    private OnClearScreen onClearScreen;
//
//    public void setOnClearScreen(OnClearScreen onClearScreen) {
//        this.onClearScreen = onClearScreen;
//    }

    public void clear(){
        mPath.reset();


        mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);



        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        curWidth = w;
        curHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        curWidth = MeasureSpec.getSize(widthMeasureSpec);
        curHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath( mPath, mPaint);
        if(needCircle){
            canvas.drawPath( circlePath,  circlePaint);
        }
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            if(needCircle){
                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        if(needCircle){
            circlePath.reset();
        }
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }


    public interface OnMoveListener{
        void onMoveStart();
        void onMove(float x, float y);
        void onMoveEnd();
    }

    private OnMoveListener onMoveListener;

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
    }

    public synchronized void refresh(){
        long curTime = System.currentTimeMillis();
        long deltaTime =  curTime - lastRefreshTime;
        if(deltaTime>= MIN_REFRESH_INTERVAL){
            invalidate();
            lastRefreshTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(onMoveListener!=null){
                    onMoveListener.onMoveStart();
                    onMoveListener.onMove(x, y);
                }
                startDraw(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if(onMoveListener!=null){
                    onMoveListener.onMove(x, y);
                }
                drawMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                if(onMoveListener!=null){
                    onMoveListener.onMove(x, y);
                    onMoveListener.onMoveEnd();
                }
                drawEnd();
                break;
        }
        return true;
    }

    public void drawEnd() {
        touch_up();
        invalidate();
    }

    public void drawMove(float x, float y) {
        touch_move(x, y);
        refresh();
    }

    public void startDraw(float x, float y) {
        touch_start(x, y);
    }

    public void setStrokeWidth(float strokeWidth){
        if(strokeWidth==0)return;
        mPaint.setStrokeWidth(strokeWidth);
    }

    public void setPaintColor(String colorString){
        if(!TextUtils.isEmpty(colorString)&&!colorString.equals("null")){
            mPaint.setColor(Color.parseColor(colorString));
        }
    }


    public boolean isNeedCircle() {
        return needCircle;
    }

    public void setNeedCircle(boolean needCircle) {
        this.needCircle = needCircle;
    }

    public int getCurWidth() {
        return curWidth;
    }

    public int getCurHeight() {
        return curHeight;
    }


}
