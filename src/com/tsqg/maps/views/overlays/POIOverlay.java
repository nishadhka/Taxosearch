package com.tsqg.maps.views.overlays;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.widget.Toast;

import com.tsqg.map.poi.POI;
import com.tsqg.maps.tileManagement.TilesManager;

public class POIOverlay extends MapOverlay
{
	protected Bitmap poiMarker;
	protected Paint bitmapPaint = new Paint();
	protected Paint textPaint = new Paint();
	protected ArrayList<POI> points = new ArrayList<POI>();
	protected ArrayList<Point> absPoints = new ArrayList<Point>();

	Context context;

	public POIOverlay(TilesManager tilesManager, Context context, Bitmap poiMarker)
	{
		super(tilesManager);

		this.context = context;
		this.poiMarker = poiMarker;

		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(30);
	}

	@Override
	protected void drawOverlay(Canvas canvas, int x, int y)
	{
		for (POI poi : points)
		{
			if (!poi.isVisible()) continue;

			Point poiPos = tilesManager.lonLatToPixelXY(poi.getLongitude(), poi.getLatitude());
			int finalX = poiPos.x + x;
			int finalY = poiPos.y + y;

			canvas.drawBitmap(poiMarker, finalX - poiMarker.getWidth() / 2, finalY - poiMarker.getHeight() / 2, bitmapPaint);
			// canvas.drawText(poi.getName(), finalX - poiMarker.getWidth() / 2,
			// finalY, textPaint);
		}
	}

	public void addPoint(POI point)
	{
		if (point == null) return;

		for (POI p : points)
		{
			if (p.getID() == point.getID()) return;
		}

		points.add(point);
		absPoints.add(tilesManager.lonLatToPixelXY(point.getLongitude(), point.getLatitude()));
	}

	@Override
	public void onClick(double longitude, double latitude)
	{
		//String str1 = "11";
		//boolean b3  = str1.equals(str1); // true
		
		Point abs = tilesManager.lonLatToPixelXY(longitude, latitude);
		float r = poiMarker.getWidth() / 2;

		for (Point p : absPoints)
		{
			if (Math.abs(abs.x- p.x) <= r && Math.abs(abs.y - p.y) <= r) 
			{
				//Math.abs(abs.x - p.x) <= r && Math.abs(abs.y - p.y) <= r&&
				//Intent i= new Intent("com.fuchs.maps.Newactivty");
//points.get (absPoints.indexOf(p)).getName() =="11"
				//((Activity) context).startActivity(i);
						
				Toast.makeText(context, points.get(absPoints.indexOf(p)).getName(), Toast.LENGTH_SHORT).show();
				return;
			}
			
		}
	}

	

	@Override
	public void onMapZoomChanged(int zoom)
	{
		absPoints.clear();
		for (POI p : points)
		{
			absPoints.add(tilesManager.lonLatToPixelXY(p.getLongitude(), p.getLatitude()));
		}

		super.onMapZoomChanged(zoom);
	}
}
