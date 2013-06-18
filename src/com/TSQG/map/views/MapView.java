package com.TSQG.map.views;

import java.util.ArrayList;
import java.util.Collection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.TSQG.map.helpers.PointD;
import com.TSQG.map.tileManagement.Tile;
import com.TSQG.map.tileManagement.TilesManager;
import com.TSQG.map.tileManagement.TilesProvider;
import com.TSQG.map.views.overlays.MapOverlay;

@SuppressLint("ViewConstructor")
public class MapView extends View
{
	protected Context context;
	protected int viewWidth, viewHeight;

	protected TilesManager tilesManager;
	protected TilesProvider tilesProvider;

	protected boolean autoFollow = false;

	protected Paint bitmapPaint = new Paint();

	protected PointD seekLocation = new PointD(0, 0);
	protected Location gpsLocation = null;

	protected int tilesSize = 256;

	protected IMapViewEventListener eventsListener;

	GestureDetector gestureDetector;

	ArrayList<MapOverlay> overlays = new ArrayList<MapOverlay>();

	public MapView(Context context, int viewWidth, int viewHeight, TilesProvider tilesProvider, int tilesSize)
	{
		super(context);
		this.context = context;

		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;

		this.tilesProvider = tilesProvider;
		this.tilesSize = tilesSize;

		tilesManager = new TilesManager(tilesSize, viewWidth, viewHeight);
		tilesManager.setZoom(1);
		tilesManager.setLocation(seekLocation.x, seekLocation.y);

		// bitmapPaint.setFilterBitmap(true);

		fetchTiles();

		MyGestureListener listener = new MyGestureListener();
		gestureDetector = new GestureDetector(context, listener);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(viewWidth, viewHeight);
	}

	Rect dst = new Rect(0, 0, 0, 0);

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawARGB(255, 100, 100, 100);

		PointD ratio = TilesManager.calcRatio(seekLocation.x, seekLocation.y);

		double tileCount = tilesManager.mapSize();
		long x = (long) (viewWidth / 2.0 - ratio.x * tileCount * tilesSize);
		long y = (long) (viewHeight / 2.0 - ratio.y * tileCount * tilesSize);

		if (tilesProvider != null)
		{
			Collection<Tile> tilesList = tilesProvider.getTiles().values();

			for (Tile tile : tilesList)
			{
				if (tile.img == null) continue;

				int tileSize = tilesManager.getTileSize();
				int finalX = (int) (tile.x * tileSize + x);
				int finalY = (int) (tile.y * tileSize + y);

				// canvas.drawBitmap(tile.img, finalX, finalY, bitmapPaint);
				dst.set(finalX, finalY, finalX + tilesSize, finalY + tilesSize);
				canvas.drawBitmap(tile.img, null, dst, bitmapPaint);
			}
		}

		for (MapOverlay overlay : overlays)
			overlay.draw(canvas, (int) x, (int) y);
	}

	void fetchTiles()
	{
		seekLocation.x = TilesManager.clampLongitude(seekLocation.x);
		seekLocation.y = TilesManager.clampLatitude(seekLocation.y);

		tilesManager.setLocation(seekLocation.x, seekLocation.y);

		Rect visibleRegion = tilesManager.getVisibleRegion();

		if (tilesProvider != null) tilesProvider.fetchTiles(visibleRegion, tilesManager.getZoom());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		gestureDetector.onTouchEvent(event);
		return true;
	}

	public void refresh()
	{
		fetchTiles();
		invalidate();
	}

	public void postRefresh()
	{
		fetchTiles();
		postInvalidate();
	}

	public void followMarker()
	{
		if (gpsLocation != null)
		{
			seekLocation.x = gpsLocation.getLongitude();
			seekLocation.y = gpsLocation.getLatitude();
			autoFollow = true;
			fetchTiles();

			// !// post?
			invalidate();
		}
	}

	public void zoomIn()
	{
		tilesManager.zoomIn();
		onMapZoomChanged();
	}

	public void zoomOut()
	{
		tilesManager.zoomOut();
		onMapZoomChanged();
	}

	protected void onMapZoomChanged()
	{
		if (tilesProvider != null) tilesProvider.clear();
		fetchTiles();

		for (MapOverlay overlay : overlays)
			overlay.onMapZoomChanged(tilesManager.getZoom());

		invalidate();
	}

	public Location getGpsLocation()
	{
		return gpsLocation;
	}

	public PointD getSeekLocation()
	{
		return seekLocation;
	}

	public void setSeekLocation(double longitude, double latitude)
	{
		seekLocation.x = longitude;
		seekLocation.y = latitude;
	}

	public void setGpsLocation(Location location)
	{
		setGpsLocation(location.getLongitude(), location.getLatitude(), location.getAltitude(), location.getAccuracy());
	}

	public void setGpsLocation(double longitude, double latitude, double altitude, float accuracy)
	{
		if (gpsLocation == null) gpsLocation = new Location("");
		gpsLocation.setLongitude(longitude);
		gpsLocation.setLatitude(latitude);
		gpsLocation.setAltitude(altitude);
		gpsLocation.setAccuracy(accuracy);

		if (autoFollow) followMarker();

		for (MapOverlay overlay : overlays)
			overlay.onLocationChange(longitude, latitude, altitude, accuracy);
	}

	public int getZoom()
	{
		return tilesManager.getZoom();
	}

	public void setZoom(int zoom)
	{
		tilesManager.setZoom(zoom);
		onMapZoomChanged();
	}

	public void addOverlay(MapOverlay overlay)
	{
		overlays.add(overlay);
	}

	public void removeOverlay(MapOverlay overlay)
	{
		overlays.remove(overlay);
	}

	public TilesManager getTilesManager()
	{
		return tilesManager;
	}

	class MyGestureListener extends SimpleOnGestureListener
	{
		private PointD getGeoPoint(float relX, float relY)
		{
			Point center = new Point(getWidth() / 2, getHeight() / 2);
			Point diff = new Point(center.x - (int) relX, center.y - (int) relY);

			Point centerGlobal = tilesManager.lonLatToPixelXY(seekLocation.x, seekLocation.y);
			centerGlobal.x -= diff.x;
			centerGlobal.y -= diff.y;

			PointD geoPoint = tilesManager.pixelXYToLatLong((int) centerGlobal.x, (int) centerGlobal.y);

			return geoPoint;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e)
		{
			PointD geoPoint = getGeoPoint(e.getX(), e.getY());
			setSeekLocation(geoPoint.x, geoPoint.y);

			zoomIn();

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e)
		{
			PointD geoPoint = getGeoPoint(e.getX(), e.getY());

			for (MapOverlay overlay : overlays)
				overlay.onClick(geoPoint.x, geoPoint.y);

			return true;
		}

		//@Override
		//public void onLongPress(MotionEvent e)
		//{
		//	PointD geoPoint = getGeoPoint(e.getX(), e.getY());
		//	if (eventsListener != null) eventsListener.onLongPress(geoPoint.x, geoPoint.y, e.getX(), e.getY());

			// !//
		//	refresh();
		//}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			autoFollow = false;

			Point diff = new Point((int) -distanceX, (int) -distanceY);

			Point pixels1 = tilesManager.lonLatToPixelXY(seekLocation.x, seekLocation.y);
			Point pixels2 = new Point(pixels1.x - diff.x, pixels1.y - diff.y);

			PointD newSeek = tilesManager.pixelXYToLatLong((int) pixels2.x, (int) pixels2.y);

			seekLocation = newSeek;

			fetchTiles();
			invalidate();

			if (eventsListener != null) eventsListener.onScroll(newSeek.x, newSeek.y, e2.getX(), e2.getY());

			return true;
		}
	}

	public void setEventsListener(IMapViewEventListener listener)
	{
		eventsListener = listener;
	}
}