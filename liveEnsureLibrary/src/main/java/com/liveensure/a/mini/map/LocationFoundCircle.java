package com.liveensure.a.mini.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

public class LocationFoundCircle extends View
{
  private ShapeDrawable mTransparentCircle;
  private ShapeDrawable mOpaqueEdge;
  private int           mWidth;
  private int           mHeight;

  public LocationFoundCircle(Context context, int size)
  {
    super(context);
    mTransparentCircle = new ShapeDrawable(new OvalShape());
    mOpaqueEdge = new ShapeDrawable(new OvalShape());
    mWidth = size;
    mHeight = size;
  }

  protected void onDraw(Canvas canvas)
  {
    int paddingX = mWidth / 3;
    int paddingY = mHeight / 3;
    int width = mWidth - (paddingX * 2);
    int height = mHeight - (paddingY * 2);

    mTransparentCircle.getPaint().setColor(0x2266AAFF); // low alpha blue
    mTransparentCircle.getPaint().setAntiAlias(true);
    mTransparentCircle.setBounds(paddingX, paddingY, paddingX + width, paddingY + height);

    mOpaqueEdge.getPaint().setColor(0x8866AAFF); // more opaque blue
    mOpaqueEdge.getPaint().setStyle(Style.STROKE);
    mOpaqueEdge.getPaint().setStrokeWidth(8);
    mOpaqueEdge.getPaint().setAntiAlias(true);
    mOpaqueEdge.setBounds(paddingX, paddingY, paddingX + width, paddingY + height);
    mTransparentCircle.draw(canvas);
    mOpaqueEdge.draw(canvas);
  }
}
