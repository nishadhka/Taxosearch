package com.fuchs.maps.views.overlays;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.fuchs.maps.helpers.PointD;
import com.fuchs.maps.tileManagement.TilesManager;

public class PathOverlay extends LocationBasedOverlay
{

	PointD dest, pos;
	Bitmap marker;
	Paint paint = new Paint();

	public PathOverlay(TilesManager tilesManager, Bitmap marker)
	{
		super(tilesManager);

		this.marker = marker;
		paint.setColor(Color.RED);
	}

	@Override
	protected void drawOverlay(Canvas canvas, int x, int y)
	{
		if (dest != null && pos != null)
		{
			Point p1 = tilesManager.lonLatToPixelXY(pos.x, pos.y);
			Point p2 = tilesManager.lonLatToPixelXY(dest.x, dest.y);

			canvas.drawLine(p1.x + x, p1.y + y, p2.x + x, p2.y + y, paint);
			canvas.drawBitmap(marker, p2.x + x - marker.getWidth() / 2f, p2.y + y - marker.getHeight() / 2f, paint);
		}
	}

	@Override
	public void onLocationChange(double longitude, double latitude, double altitude, float accuracy)
	{
		if (pos == null) pos = new PointD();
		pos.x = longitude;
		pos.y = latitude;
	}

	public void setDestenation(PointD dest)
	{
		this.dest = dest;
	}

}
