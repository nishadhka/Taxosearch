package com.fuchs.maps.tileManagement;

import android.graphics.Bitmap;

public class Tile
{
	public int x;
	public int y;
	public Bitmap img;

	public Tile(int x, int y, Bitmap img)
	{
		this.x = x;
		this.y = y;
		this.img = img;			
	}

	@Override
	public String toString()
	{
		return x + "," + y;
	}
}