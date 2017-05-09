package com.hgxx.whiteboard.views.drawview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.hgxx.whiteboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 08/05/2017.
 */

public class DrawLayout extends RelativeLayout {
     public static final int MIN_REFRESH_INTERVAL = 30;
    private long lastRefreshTime;

//    private Bitmap mBitmap;
//    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Paint mPaint;
    private int curWidth;
    private int curHeight;

    private boolean needCircle = true;
    private boolean drawable = true;


    private List<PathObject> paths = new ArrayList<>();
    private String mPaintColor = "#000000";
    private float mStrokeWidth = 12;

    class PathObject {
        Path path;
        String color;
        float strokeWidth;
    }


    public DrawLayout(Context c) {
        this(c, null);
    }

    public DrawLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        mBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(mBitmap);

        this.context=context;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        if(needCircle){
            initCircle();
        }
        initPaint();
//        mBitmap = Bitmap.createBitmap(500, 300, Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(mBitmap);
        getAttributes(context, attrs, defStyleAttr);
    }

    private void getAttributes(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.DrawView, defStyleAttr, 0);
        String paintColor = customAttrs.getString(R.styleable.DrawView_paintColor);
        int paintWidth = (int)customAttrs.getDimension(R.styleable.DrawView_paintWidth,0);
        boolean needCircle = customAttrs.getBoolean(R.styleable.DrawView_needCircle, true);
        boolean drawable = customAttrs.getBoolean(R.styleable.DrawView_drawable, true);


        if(!TextUtils.isEmpty(paintColor)){
            setPaintColor(paintColor);
        }
        if(paintWidth!=0){
            setStrokeWidth(paintWidth);
        }
        setNeedCircle(needCircle);
        setDrawable(drawable);
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
        mPaint.setColor(Color.BLACK);
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
        paths.clear();
        mPath.reset();
        //TODO clear
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(w>0&&h>0){
            curWidth = w;
            curHeight = h;

//            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//            mCanvas = new Canvas(mBitmap);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int msHeight = MeasureSpec.makeMeasureSpec(500000, MeasureSpec.EXACTLY);
        int msWidth = MeasureSpec.makeMeasureSpec(curWidth, MeasureSpec.EXACTLY);


        setMeasuredDimension(msWidth, msHeight);
        curWidth = MeasureSpec.getSize(widthMeasureSpec);
        curHeight = MeasureSpec.getSize(heightMeasureSpec);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);


        for(int i=0;i<paths.size();i++){
            PathObject pathObject = paths.get(i);
            mPaint.setColor(Color.parseColor(pathObject.color));
            mPaint.setStrokeWidth(pathObject.strokeWidth);
            canvas.drawPath(pathObject.path, mPaint);
            pathObject.path.isEmpty();


        }


        mPaint.setColor(Color.parseColor(mPaintColor));
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawPath(mPath, mPaint);

        if(needCircle){
            canvas.drawPath( circlePath,  circlePaint);
        }
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
//        mPath.reset();
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
//        if(mCanvas!=null){
//            mCanvas.drawPath(mPath, mPaint);
//        }
        // kill this so we don't double draw
        PathObject po = new PathObject();
        po.color = mPaintColor;
        po.path = mPath;
        po.strokeWidth = mStrokeWidth;
        paths.add(po);
        mPath = new Path();
//        mPath.reset();
    }

    public void touch_cancel(int cancelIndex){
        if(paths.size()<=cancelIndex&&cancelIndex>=0)return;
        if(cancelIndex==-1){
            paths = paths.subList(0, paths.size()-1);
        }
        else if(cancelIndex>0){
            paths = paths.subList(0, cancelIndex);
        }
        invalidate();
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

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if(!drawable)return super.onTouchEvent(event);
//        float x = event.getX();
//        float y = event.getY();
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                if(onMoveListener!=null){
//                    onMoveListener.onMoveStart();
//                    onMoveListener.onMove(x, y);
//                }
//                startDraw(x, y);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if(onMoveListener!=null){
//                    onMoveListener.onMove(x, y);
//                }
//                drawMove(x, y);
//                break;
//            case MotionEvent.ACTION_UP:
//                if(onMoveListener!=null){
//                    onMoveListener.onMove(x, y);
//                    onMoveListener.onMoveEnd();
//                }
//                drawEnd();
//                break;
//        }
//        return true;
//    }



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
        mStrokeWidth = strokeWidth;
        mPaint.setStrokeWidth(strokeWidth);
    }

    public void setPaintColor(String colorString){
        mPaintColor = colorString;
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

    public void setCurWidth(int curWidth) {
        this.curWidth = curWidth;
    }

    public void setCurHeight(int curHeight) {
        this.curHeight = curHeight;
    }

    public boolean isDrawable() {
        return drawable;
    }

    public void setDrawable(boolean drawable) {
        this.drawable = drawable;
    }
}
