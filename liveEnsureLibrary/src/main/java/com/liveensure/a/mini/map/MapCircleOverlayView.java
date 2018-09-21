package com.liveensure.a.mini.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

import com.google.android.gms.maps.MapView;

public class MapCircleOverlayView extends View {
	private ShapeDrawable mTransparentCircle;
	private ShapeDrawable mOpaqueEdge;
	private Paint mCrosshairPaint = new Paint();
	private MapView mMapView;
	private int centerX;
	private int centerY;
	private float densityScale;

	//private static final String TAG = MapCircleOverlayView.class.getSimpleName();

	public MapCircleOverlayView(Context context, int mapWidth, int mapHeight, float densityScale) {
		super(context);

		int paddingX = 25;
		int paddingY = 25;
		int width = mapWidth - (paddingX * 2);
		int height = mapHeight - (paddingY * 2);
		centerX = mapWidth / 2;
		centerY = mapHeight / 2;
		this.densityScale = densityScale;

		mTransparentCircle = new ShapeDrawable(new OvalShape());
		mTransparentCircle.getPaint().setColor(0x2266AAFF); // low alpha blue
		mTransparentCircle.getPaint().setAntiAlias(true);
		mTransparentCircle.setBounds(paddingX, paddingY, paddingX + width, paddingY + height);
		
		mOpaqueEdge = new ShapeDrawable(new OvalShape());
		mOpaqueEdge.getPaint().setColor(0x8866AAFF); // more opaque blue
		mOpaqueEdge.getPaint().setStyle(Style.STROKE);
		mOpaqueEdge.getPaint().setStrokeWidth(4 * densityScale);
		mOpaqueEdge.getPaint().setAntiAlias(true);
		mOpaqueEdge.setBounds(paddingX, paddingY, paddingX + width, paddingY + height);

		mCrosshairPaint.setColor(0x8866AAFF);
		mCrosshairPaint.setStrokeWidth(2 * densityScale);
	}
	
	public void setMapView(MapView mapView) {
		mMapView = mapView;
	}
	
	private void updateBounds() {
		if (mMapView == null) {
			return;
		}
		int padding = 5;
		
		int left = mMapView.getLeft() + padding;
		int top = mMapView.getTop() + padding;
		int width = mMapView.getWidth() - padding * 2;
		int height = mMapView.getHeight() - padding * 2;
		
		if (width < height) {
			// Map is a tall rect, so make the radius the width and adjust the top position
			top = top + (height - width) / 2;
			height = width;
		} else {
			// Map is a wide rect, so make the radius the height and adjust the left position
			left = left + (width - height) / 2;
			width = height;
		}
		
		int cx1 = left;
		int cy1 = top;
		int cx2 = left + width;
		int cy2 = top + height;
		
		centerX = cx1 + (cx2 - cx1) / 2;
		centerY = cy1 + (cy2 - cy1) / 2;
		
		mTransparentCircle.setBounds(cx1, cy1, cx2, cy2);
		mOpaqueEdge.setBounds(cx1, cy1, cx2, cy2);
	}
	
	protected void onDraw(Canvas canvas) {
		updateBounds();
		mTransparentCircle.draw(canvas);
		mOpaqueEdge.draw(canvas);
		canvas.drawLine(centerX - 15 * densityScale, centerY, centerX + 15 * densityScale, centerY, mCrosshairPaint);
		canvas.drawLine(centerX, centerY - 15 * densityScale, centerX, centerY + 15 * densityScale, mCrosshairPaint);
	}

}
