package com.tsqg.maps.tileManagement;

import android.graphics.Point;
import android.graphics.Rect;

import com.tsqg.maps.helpers.PointD;

public class TilesManager
{
	public final static double EarthRadius = 6378137;
	public final static double MinLatitude = -85.05112878; // South pole
	public final static double MaxLatitude = 85.05112878; // North pole
	public final static double MinLongitude = -180; // West
	public final static double MaxLongitude = 180; // East

	public final static double MaxMinLatitude = (MaxLatitude - MinLatitude);
	public final static double MaxMinLongitude = (MaxLongitude - MinLongitude);

	protected int maxZoom = 19;

	protected int mapSize(int zoom)
	{
		return (int) Math.pow(tileSize, zoom);
	}

	protected int tileSize = 256;

	protected int viewWidth = 800;
	protected int viewHeight = 600;

	protected int tileCountX = 3;
	protected int tileCountY = 5;

	protected Rect visibleRegion;

	// Map state
	protected PointD location = new PointD(MinLongitude, MinLatitude);
	protected int zoom = 0;

	// Map state

	public TilesManager(int tileSize, int viewWidth, int viewHeight)
	{
		this.tileSize = tileSize;

		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;

		setViewDimensions(viewWidth, viewHeight);
	}

	protected static double clamp(double x, double min, double max)
	{
		return Math.min(Math.max(x, min), max);
	}

	public static double clampLatitude(double latitude)
	{
		return clamp(latitude, MinLatitude, MaxLatitude);
	}

	public static double clampLongitude(double longitude)
	{
		return clamp(longitude, MinLongitude, MaxLongitude);
	}

	public static PointD calcRatio(double longitude, double latitude)
	{
		double ratioX = ((longitude + 180.0) / 360.0);

		double sinLatitude = Math.sin(latitude * Math.PI / 180.0);
		double ratioY = (0.5 - Math.log((1 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI));

		return new PointD(ratioX, ratioY);
	}

	public int mapSize()
	{
		return (int) Math.pow(2, zoom);
	}

	public double calcGroundResolution(double latitude)
	{
		latitude = clampLatitude(latitude);
		// !//
		return Math.cos(latitude * Math.PI / 180.0) * 2.0 * Math.PI * EarthRadius / (double) (tileSize * mapSize());
	}

	public Point lonLatToPixelXY(double longitude, double latitude)
	{
		longitude = clamp(longitude, MinLongitude, MaxLongitude);
		latitude = clamp(latitude, MinLatitude, MaxLatitude);

		PointD ratio = calcRatio(longitude, latitude);
		double x = ratio.x;
		double y = ratio.y;

		long mapSize = mapSize() * tileSize;
		int pixelX = (int) clamp(x * mapSize + 0.5, 0, mapSize - 1);
		int pixelY = (int) clamp(y * mapSize + 0.5, 0, mapSize - 1);

		return new Point(pixelX, pixelY);
	}

	protected Point calcTileIndices(double longitude, double latitude)
	{
		PointD ratio = calcRatio(longitude, latitude);
		double ratioX = ratio.x;
		double ratioY = ratio.y;

		int tile1DCount = mapSize();

		return new Point((int) (ratioX * tile1DCount), (int) (ratioY * tile1DCount));
	}

	public PointD pixelXYToLatLong(int pixelX, int pixelY)
	{
		double mapSize = mapSize() * tileSize;
		double x = (clamp(pixelX, 0, mapSize - 1) / mapSize) - 0.5;
		double y = 0.5 - (clamp(pixelY, 0, mapSize - 1) / mapSize);

		double latitude = 90.0 - 360.0 * Math.atan(Math.exp(-y * 2.0 * Math.PI)) / Math.PI;
		double longitude = 360.0 * x;

		return new PointD(longitude, latitude);
	}

	protected void updateVisibleRegion(double longitude, double latitude, int zoom)
	{
		location.x = longitude;
		location.y = latitude;
		this.zoom = zoom;

		Point tileIndex = calcTileIndices(location.x, location.y);
		int halfTileCountX = (int) ((float) (tileCountX + 1) / 2f);
		int halfTileCountY = (int) ((float) (tileCountY + 1) / 2f);

		visibleRegion = new Rect(tileIndex.x - halfTileCountX, tileIndex.y - halfTileCountY, tileIndex.x + halfTileCountX, tileIndex.y
				+ halfTileCountY);
	}

	public void setZoom(int zoom)
	{
		zoom = (int) clamp(zoom, 0, maxZoom);

		updateVisibleRegion(location.x, location.y, zoom);
	}

	public void setLocation(double longitude, double latitude)
	{
		updateVisibleRegion(longitude, latitude, zoom);
	}

	public void setLocationByRatio(double x, double y)
	{
		double longitude = (x * MaxMinLongitude) + MinLongitude;
		double latitude = -1.0 * (y * MaxMinLatitude) + MinLatitude;

		updateVisibleRegion(longitude, latitude, zoom);
	}

	public void setViewDimensions(int width, int height)
	{
		viewWidth = width;
		viewHeight = height;

		tileCountX = (int) ((float) width / tileSize) + 1;
		tileCountY = (int) ((float) height / tileSize) + 1;

		updateVisibleRegion(location.x, location.y, zoom);
	}

	public Rect getVisibleRegion()
	{
		return visibleRegion;
	}

	public int getZoom()
	{
		return zoom;
	}

	public int zoomIn()
	{
		setZoom(zoom + 1);
		return zoom;
	}

	public int zoomOut()
	{
		setZoom(zoom - 1);
		return zoom;
	}

	public int getTileSize()
	{
		return tileSize;
	}

	public int getMaxZoom()
	{
		return maxZoom;
	}

	public void setMaxZoom(int maxZoom)
	{
		this.maxZoom = maxZoom;
	}

}