package com.tsqg.maps.views;

public interface IMapViewEventListener
{
	public boolean onLongPress(double lon, double lat, float relX, float relY);

	public boolean onScroll(double lon, double lat, float relX, float relY);
}
