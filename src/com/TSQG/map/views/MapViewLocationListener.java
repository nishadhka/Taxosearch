package com.TSQG.map.views;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;

public class MapViewLocationListener implements LocationListener
{
	MapView mapView;
	Handler gpsStateChangeHandler;
	boolean stopped = false;

	public MapViewLocationListener(MapView mapView, Handler gpsStateChangeHandler)
	{
		this.mapView = mapView;
		this.gpsStateChangeHandler = gpsStateChangeHandler;
	}

	@Override
	public void onLocationChanged(Location location)
	{
		if (!stopped && location != null)
		{
			mapView.setGpsLocation(location.getLongitude(), location.getLatitude(), location.getAltitude(), location.getAccuracy());
			mapView.postInvalidate();
		}
	}

	public void stop()
	{
		stopped = true;
		mapView = null;
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		if (gpsStateChangeHandler != null) gpsStateChangeHandler.sendEmptyMessage(LocationProvider.OUT_OF_SERVICE);
	}

	@Override
	public void onProviderEnabled(String provider)
	{		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		if (gpsStateChangeHandler != null) gpsStateChangeHandler.sendEmptyMessage(status);
	}

}