package com.fuchs.maps.views.overlays;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.fuchs.maps.tileManagement.TilesManager;

public class PositionMarkerOverlay extends LocationBasedOverlay
{
	protected Bitmap positionMarker;
	protected Paint bitmapPaint = new Paint();
	protected Paint circlePaint = new Paint();

	protected boolean fixAvailable = false;

	public PositionMarkerOverlay(TilesManager tilesManager, Bitmap positionMarker)
	{
		super(tilesManager);

		this.positionMarker = positionMarker;

		circlePaint.setARGB(70, 170, 170, 80);
		circlePaint.setAntiAlias(true);
	}

	@Override
	protected void drawOverlay(Canvas canvas, int x, int y)
	{
		if (!fixAvailable) return;

		Point markerPos = tilesManager.lonLatToPixelXY(longitude, latitude);

		int markerX = (int) (markerPos.x + x);
		int markerY = (int) (markerPos.y + y);

		canvas.drawBitmap(positionMarker, markerX - positionMarker.getWidth() / 2, markerY - positionMarker.getHeight() / 2, bitmapPaint);

		float ground = (float) tilesManager.calcGroundResolution(latitude);
		float rad = accuracy / ground;
		canvas.drawCircle(markerX, markerY, rad, circlePaint);
	}

	@Override
	public void onLocationChange(double longitude, double latitude, double altitude, float accuracy)
	{
		super.onLocationChange(longitude, latitude, altitude, accuracy);
		fixAvailable = true;
	}
}
