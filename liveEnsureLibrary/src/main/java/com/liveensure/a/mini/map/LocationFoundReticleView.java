package com.liveensure.a.mini.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;

public class LocationFoundReticleView extends View
{
  //  private static final String TAG = LocationFoundReticleView.class.getSimpleName();
  private Paint         mTrianglePaint;
  private Path          mTopTriangle;
  private Path          mBottomTriangle;
  private Path          mLeftTriangle;
  private Path          mRightTriangle;
  private ShapeDrawable mCenterDotCircle;
  private ShapeDrawable mCenterDotEdge;
  private int           mWidth;
  private int           mHeight;

  public LocationFoundReticleView(Context context, int size)
  {
    super(context);
    mWidth = size;
    mHeight = size;
    init(context);
  }

  public LocationFoundReticleView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    init(context);
  }

  public LocationFoundReticleView(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    init(context);
  }

  public void init(Context context)
  {
    setFocusable(true);
    mTrianglePaint = new Paint();
    mCenterDotEdge = new ShapeDrawable(new OvalShape());
    mCenterDotCircle = new ShapeDrawable(new OvalShape());
    mTopTriangle = new Path();
    mBottomTriangle = new Path();
    mLeftTriangle = new Path();
    mRightTriangle = new Path();
  }

  protected void onDraw(Canvas canvas)
  {
    int paddingX = mWidth / 3;
    int paddingY = mHeight / 3;
    int width = mWidth - (paddingX * 2);
    int height = mHeight - (paddingY * 2);
    float triangleHeight = height / 4;
    float triangleWidth = triangleHeight / 2;
    int centerDotRadius = mHeight / 15;

    float topTriangleInnerPointX = paddingX + (width / 2);
    float topTriangleInnerPointY = (paddingY + (height / 2)) - (height / 4);
    float bottomTriangleInnerPointX = paddingX + (width / 2);
    float bottomTriangleInnerPointY = (paddingY + (height / 2)) + (height / 4);
    float leftTriangleInnerPointX = paddingX + (width / 2) - (width / 4);
    float leftTriangleInnerPointY = (paddingY + (height / 2));
    float rightTriangleInnerPointX = paddingX + (width / 2) + (width / 4);
    float rightTriangleInnerPointY = (paddingY + (height / 2));
    int centerDotX = paddingX + (width / 2) - (centerDotRadius / 2);
    int centerDotY = paddingY + (height / 2) - (centerDotRadius / 2);

    mTrianglePaint.setColor(0xDD66AAFF);

    mCenterDotCircle.getPaint().setColor(mTrianglePaint.getColor());
    mCenterDotCircle.setBounds(centerDotX, centerDotY, centerDotX + centerDotRadius, centerDotY + centerDotRadius);

    mCenterDotEdge.getPaint().setColor(0xEEEEEEFF); // very light blue
    mCenterDotEdge.getPaint().setStyle(Style.STROKE);
    mCenterDotEdge.getPaint().setStrokeWidth(6);
    mCenterDotEdge.getPaint().setAntiAlias(true);
    mCenterDotEdge.setBounds(centerDotX, centerDotY, centerDotX + centerDotRadius, centerDotY + centerDotRadius);

    mTopTriangle.moveTo(topTriangleInnerPointX, topTriangleInnerPointY);
    mTopTriangle.lineTo(topTriangleInnerPointX - (triangleWidth / 2), topTriangleInnerPointY - triangleHeight);
    mTopTriangle.lineTo(topTriangleInnerPointX + (triangleWidth / 2), topTriangleInnerPointY - triangleHeight);
    mTopTriangle.lineTo(topTriangleInnerPointX, topTriangleInnerPointY);

    mBottomTriangle.moveTo(bottomTriangleInnerPointX, bottomTriangleInnerPointY);
    mBottomTriangle.lineTo(bottomTriangleInnerPointX - (triangleWidth / 2), bottomTriangleInnerPointY + triangleHeight);
    mBottomTriangle.lineTo(bottomTriangleInnerPointX + (triangleWidth / 2), bottomTriangleInnerPointY + triangleHeight);
    mBottomTriangle.lineTo(bottomTriangleInnerPointX, bottomTriangleInnerPointY);

    mLeftTriangle.moveTo(leftTriangleInnerPointX, leftTriangleInnerPointY);
    mLeftTriangle.lineTo(leftTriangleInnerPointX - triangleHeight, leftTriangleInnerPointY + (triangleWidth / 2));
    mLeftTriangle.lineTo(leftTriangleInnerPointX - triangleHeight, leftTriangleInnerPointY - (triangleWidth / 2));
    mLeftTriangle.lineTo(leftTriangleInnerPointX, leftTriangleInnerPointY);

    mRightTriangle.moveTo(rightTriangleInnerPointX, rightTriangleInnerPointY);
    mRightTriangle.lineTo(rightTriangleInnerPointX + triangleHeight, rightTriangleInnerPointY - (triangleWidth / 2));
    mRightTriangle.lineTo(rightTriangleInnerPointX + triangleHeight, rightTriangleInnerPointY + (triangleWidth / 2));
    mRightTriangle.lineTo(rightTriangleInnerPointX, rightTriangleInnerPointY);

    mCenterDotCircle.draw(canvas);
    mCenterDotEdge.draw(canvas);
    canvas.drawPath(mTopTriangle, mTrianglePaint);
    canvas.drawPath(mBottomTriangle, mTrianglePaint);
    canvas.drawPath(mLeftTriangle, mTrianglePaint);
    canvas.drawPath(mRightTriangle, mTrianglePaint);
  }
}
