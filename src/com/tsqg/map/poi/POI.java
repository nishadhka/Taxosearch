package com.tsqg.map.poi;

import java.io.Serializable;

import com.tsqg.maps.helpers.PointD;

public class POI implements Serializable
{
	private static final long serialVersionUID = -8618762943599351669L;

	private long id;
	private String name;
	private double longitude;
	private double latitude;
	private boolean visible = true;

	public POI(long id, String name, double longitude, double latitude)
	{
		this(id, name, longitude, latitude, true);
	}

	public POI(long id, String name, double longitude, double latitude, boolean visible)
	{
		this.id = id;
		this.name = name;
		this.longitude = longitude;
		this.latitude = latitude;
		this.visible = visible;
	}

	public long getID()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean value)
	{
		visible = value;
	}

	public PointD getPointD()
	{
		return new PointD(longitude, latitude);
	}

	@Override
	public String toString()
	{
		return id + ":" + name;
	}
}
