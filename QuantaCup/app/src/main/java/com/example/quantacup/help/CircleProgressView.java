package com.example.quantacup.help;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class CircleProgressView extends View {

    private int mMaxProgress = 100;
    private int Progress = 0;
    private int Progress1 = 0;
    private int Progress2 = 0;

    private final int mCircleLineStrokeWidth = 100;//设置圆形画笔宽度

    private final int mTxtStrokeWidth =  2;//设置文字画笔宽度

    // 画圆所在的距形区域
    private  RectF mRectF;
    private  Paint mPaint;



    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs,0);

        mRectF = new RectF();
        mPaint = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        // 设置画笔相关属性
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        canvas.drawColor(Color.TRANSPARENT);
        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);

        // 位置
        mRectF.left = mCircleLineStrokeWidth / 2;
        mRectF.top = mCircleLineStrokeWidth / 2;
        mRectF.right = width - mCircleLineStrokeWidth / 2;
        mRectF.bottom = height - mCircleLineStrokeWidth / 2;

        // 绘制圆圈，进度条背景
        canvas.drawArc(mRectF, -180, 360, false, mPaint);
        mPaint.setColor(Color.RED);
        canvas.drawArc(mRectF, -180, ((float) Progress / mMaxProgress) * 360, false, mPaint);


        //绘制第二段长度
        mPaint.setColor(Color.BLUE);
        canvas.drawArc(mRectF,-180+ ((float) Progress / mMaxProgress) * 360, ((float) Progress1 / mMaxProgress) * 360, false, mPaint);

        //绘制第三段长度
        mPaint.setColor(Color.BLACK);
        canvas.drawArc(mRectF,-180+ ((float) (Progress+Progress1) / mMaxProgress) * 360, ((float) Progress2 / mMaxProgress) * 360, false, mPaint);



        //绘制百分百
        mPaint.setStrokeWidth(mTxtStrokeWidth);
        String text = Progress + "%";
        int textHeight = height / 4;
        mPaint.setTextSize(textHeight);
        int textWidth = (int) mPaint.measureText(text, 0, text.length());
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + textHeight / 2, mPaint);

    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.Progress = progress;
        this.invalidate();
    }

    public void setProgress1(int progress) {
        this.Progress1 = progress;
        this.invalidate();
    }

    public void setProgress2(int progress) {
        this.Progress2 = progress;
        this.invalidate();
    }

}


