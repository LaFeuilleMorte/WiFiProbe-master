package com.example.administrator.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

//public class MyCircle extends View {
  /*  private Paint mPaint=new Paint();
    /**
     * 2.初始化画笔
     */
  /*  private void initPaint() {
        mPaint = new Paint();
        //设置画笔颜色
        mPaint.setColor(Color.BLACK);
        //STROKE                //描边
        //FILL                  //填充
        //FILL_AND_STROKE       //描边加填充
        //设置画笔模式
        mPaint.setStyle(Paint.Style.FILL);
        //设置画笔宽度为30px
        mPaint.setStrokeWidth(30f);
    }
    public MyCircle(Context context){
        super(context);
    }
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        initPaint();
        canvas.drawCircle(500,500,400,mPaint);
    }
    */

//}
public class BullsView extends View {

    private Paint mPaint;
    private float distance=0f;
    private Point mCenter;
    private float mRadius;
    private DataSource dataSource=DataSource.getInsDs();
    private Bitmap bitmap=BitmapFactory.decodeResource(this.getResources(),R.drawable.arrow);
    public BullsView(Context context) {
        this(context, null);
    }

    public BullsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BullsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 创建画笔（支持锯齿）
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        // 创建圆心
        mCenter = new Point();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width, height;
        // 确定内容的理想大小，无约束
        int cWidth = 100;
        int mHeight = 100;

        width = getHowToGetWH(widthMeasureSpec, cWidth);
        height = getHowToGetWH(heightMeasureSpec, mHeight);

        // 使用测量必须调用该方法
        setMeasuredDimension(width, height);
    }

    /**
     * 测量宽度和高度的方法
     */
    private int getHowToGetWH(int measureSpec, int mSize) {

        int specSize = MeasureSpec.getSize(measureSpec);

        switch (MeasureSpec.getMode(measureSpec)){
            case MeasureSpec.AT_MOST:
                return Math.min(specSize, mSize);
            case MeasureSpec.UNSPECIFIED:
                return mSize;
            case MeasureSpec.EXACTLY:
                return specSize;
            default:
                return 0;
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        // 如果有变化，则复位参数
        if (w != oldw || h != oldh){
            mCenter.x = w/2;
            mCenter.y = h/2;
            mRadius = Math.min(mCenter.x, mCenter.y);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 绘制同心圆
        mPaint.setColor(Color.RED);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius * 0.98f, mPaint);

        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius * 0.67f, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius * 0.65f, mPaint);

        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius * 0.4f, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius * 0.38f, mPaint);

        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius * 0.2f, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius * 0.18f, mPaint);

        mPaint.setColor(Color.BLACK);
        canvas.drawLine(mCenter.x,mCenter.y-mRadius,mCenter.x,mCenter.y+mRadius,mPaint);

        mPaint.setColor(Color.BLACK);
        canvas.drawLine(mCenter.x-mRadius,mCenter.y,mCenter.x+mRadius,mCenter.y,mPaint);
        //mPaint.setColor(Color.BLUE);
        //canvas.drawCircle(mCenter.x, mCenter.y, mRadius * 0.03f, mPaint);

        mPaint.setColor(Color.RED);
        canvas.drawCircle(mCenter.x+(float)dataSource.getLon()*1100*mRadius,mCenter.y +(float)dataSource.getLat()*1100*mRadius,mRadius*0.03f,mPaint);
        //Log.d("refresh",(float) mCenter.y-(dataSource.getValue())/100*mRadius+"confirmed");
    }
}
  class DataSource{
    double lat=0;
    double lon=0;
    double angle=0;
    static DataSource dataSource=null;
    public static DataSource getInsDs(){
        if(dataSource==null){
            dataSource=new DataSource();
        }
        return dataSource;
    }
    private DataSource(){

    }
    public void setLat(double lat){
        this.lat=lat;
    }

    public double getLat() {
        Log.d("输出值大小",lat+"");
        return this.lat;
    }


      public void setLon(double lon) {
          this.lon = lon;
      }

      public double getLon() {
          return lon;
      }

      public void setAngle(double angle){
        this.angle=angle;
    }

      public double getAngle() {
          return angle;
      }
  }