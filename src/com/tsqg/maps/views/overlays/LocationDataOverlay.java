package com.tsqg.maps.views.overlays;

import com.tsqg.maps.tileManagement.TilesManager;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class LocationDataOverlay extends LocationBasedOverlay
{

	protected Paint fontPaint;

	public LocationDataOverlay(TilesManager tilesManager)
	{
		super(tilesManager);

		fontPaint = new Paint();
		fontPaint.setColor(Color.DKGRAY);
		fontPaint.setShadowLayer(1, 1, 1, Color.BLACK);
		fontPaint.setTextSize(20);
	}

	@Override
	protected void drawOverlay(Canvas canvas, int x, int y)
	{
		int pen = 1;
		// canvas.drawText("Fetching:" + fetchTime, 0, 20 * pen++, fontPaint);
		// canvas.drawText("Rendering:" + renderTime, 0, 20 * pen++, fontPaint);
		canvas.drawText("Zoom:" + tilesManager.getZoom(), 0, 20 * pen++, fontPaint);

		float ground = (float) tilesManager.calcGroundResolution(latitude);
		canvas.drawText("------------", 0, 20 * pen++, fontPaint);
		canvas.drawText("lon:" + longitude, 0, 20 * pen++, fontPaint);
		canvas.drawText("lat:" + latitude, 0, 20 * pen++, fontPaint);
		canvas.drawText("alt:" + altitude, 0, 20 * pen++, fontPaint);
		canvas.drawText("acc:" + accuracy, 0, 20 * pen++, fontPaint);
		canvas.drawText("ground:" + ground, 0, 20 * pen++, fontPaint);
	}
}
