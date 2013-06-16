package com.fuchs.maps.views.overlays;

import com.fuchs.maps.tileManagement.TilesManager;

abstract public class LocationBasedOverlay extends MapOverlay
{

	protected double longitude, latitude, altitude;
	protected float accuracy;

	public LocationBasedOverlay(TilesManager tilesManager)
	{
		super(tilesManager);
	}

	@Override
	public void onLocationChange(double longitude, double latitude, double altitude, float accuracy)
	{
		this.longitude = longitude;
		this.latitude = latitude;
		this.accuracy = accuracy;
		this.altitude = altitude;
	}

}
