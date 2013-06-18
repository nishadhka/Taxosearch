package com.TSQG.map;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.TSQG.map.helpers.PointD;
import com.TSQG.map.poi.POI;
import com.TSQG.map.poi.POIProvider;
import com.TSQG.map.views.MapView;
import com.TSQG.map.views.overlays.POIOverlay;
import com.TSQG.map.views.overlays.PathOverlay;


public class StateManager
{
	private static final class Save
	{
		public final static String GPS_LON = "gpsLon";
		public final static String GPS_LAT = "gpsLAT";
		public final static String GPS_ALT = "gpsALT";
		public final static String GPS_ACC = "gpsAcc";
	}

	protected static void onSaveInstanceState(Bundle outState, MapView mapView)
	{
		if (mapView.getGpsLocation() != null)
		{
			outState.putDouble(Save.GPS_LON, mapView.getGpsLocation().getLongitude());
			outState.putDouble(Save.GPS_LAT, mapView.getGpsLocation().getLatitude());
			outState.putDouble(Save.GPS_ALT, mapView.getGpsLocation().getAltitude());
			outState.putFloat(Save.GPS_ACC, mapView.getGpsLocation().getAccuracy());
		}
	}

	protected static Location onRestoreInstanceState(Bundle savedInstanceState)
	{
		double gpsLon, gpsLat, gpsAlt;
		float gpsAcc;

		gpsLon = savedInstanceState.getDouble(Save.GPS_LON, 999);
		gpsLat = savedInstanceState.getDouble(Save.GPS_LAT, 999);
		gpsAlt = savedInstanceState.getDouble(Save.GPS_ALT, 999);
		gpsAcc = savedInstanceState.getFloat(Save.GPS_ACC, 999);

		Location outLocation = null;
		if (gpsLon != 999 && gpsLat != 999 && gpsAlt != 999 && gpsAcc != 999)
		{
			outLocation = new Location(LocationManager.GPS_PROVIDER);
			outLocation.setLongitude(gpsLon);
			outLocation.setLatitude(gpsLat);
			outLocation.setAltitude(gpsAlt);
			outLocation.setAccuracy(gpsAcc);
		}

		return outLocation;
	}

	protected static void restoreMapViewPreferences(Context context, MapView mapView)
	{
		double lon = MyPreferences.getViewSeekLon(context);
		double lat = MyPreferences.getViewSeekLat(context);
		int zoom = MyPreferences.getViewZoom(context);

		mapView.setSeekLocation(lon, lat);
		mapView.setZoom(zoom);
		mapView.refresh();
	}

	protected static void saveMapViewPreferences(Context context, MapView mapView)
	{
		PointD seekLocation = mapView.getSeekLocation();
		MyPreferences.setViewSeek(context, seekLocation, mapView.getZoom());
	}

	protected static void restorePOIOverlayState(Context context, POIOverlay poiOverlay)
	{
		ArrayList<POI> points = POIProvider.getAllPoints(context, true);

		for (POI point : points)
			poiOverlay.addPoint(point);
	}

	public static void restorePathOverlayState(Context context, PathOverlay pathOverlay)
	{
		PointD dest = MyPreferences.getDestination(context);
		pathOverlay.setDestenation(dest);
	}

	public static boolean destinationExists(Context context)
	{
		return (MyPreferences.getDestination(context) != null);
	}
}
