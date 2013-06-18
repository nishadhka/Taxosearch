package com.TSQG.map.views.overlays;

import com.TSQG.map.tileManagement.TilesManager;

import android.graphics.Canvas;

public abstract class MapOverlay
{
	protected TilesManager tilesManager;
	protected boolean visible = true;

	public MapOverlay(TilesManager tilesManager)
	{
		this.tilesManager = tilesManager;
	}

	public void draw(Canvas canvas, int x, int y)
	{
		if (visible) drawOverlay(canvas, x, y);
	}

	protected abstract void drawOverlay(Canvas canvas, int x, int y);

	public void onMapZoomChanged(int zoom)
	{
	}

	public void onLocationChange(double longitude, double latitude, double altitude, float accuracy)
	{
	}

	public void onClick(double longitude, double latitude)
	{
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
}
