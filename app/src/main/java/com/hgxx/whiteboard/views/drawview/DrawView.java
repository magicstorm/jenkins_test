package com.hgxx.whiteboard.views.drawview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

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

    public DrawView(Context c) {
        super(c);
        context=c;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
        initPaint();
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

        if(mBitmap!=null&&!mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        if(mBitmap!=null&&!mBitmap.isRecycled()){
            mBitmap.recycle();
            mBitmap=null;
        }
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
        canvas.drawPath( circlePath,  circlePaint);
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

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
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



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onMoveListener.onMoveStart();
                onMoveListener.onMove(x, y);
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onMoveListener.onMove(x, y);
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onMoveListener.onMove(x, y);
                onMoveListener.onMoveEnd();
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void setPaintColor(String colorString){
        mPaint.setColor(Color.parseColor(colorString));
    }

    public int getCurWidth() {
        return curWidth;
    }

    public void setCurWidth(int curWidth) {
        this.curWidth = curWidth;
    }

    public int getCurHeight() {
        return curHeight;
    }

    public void setCurHeight(int curHeight) {
        this.curHeight = curHeight;
    }


}
