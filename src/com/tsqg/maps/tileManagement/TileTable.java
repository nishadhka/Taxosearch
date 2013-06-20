package com.tsqg.maps.tileManagement;

import java.util.ArrayList;
import java.util.Hashtable;


public class TileTable extends Hashtable<Integer, Hashtable<Integer, Tile>>
{
	private static final long serialVersionUID = 8282098262291188772L;

	public Tile get(int x, int y)
	{
		// !//
		try
		{			
			return super.get(x).get(y);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void put(int x, int y, Tile tile)
	{
		Hashtable<Integer, Tile> map = super.get(x);

		if (map == null)
		{
			map = new Hashtable<Integer, Tile>();
			super.put(x, map);
		}

		map.put(y, tile);
	}

	public boolean exists(int x, int y)
	{
		return (super.containsKey(x) && super.get(x).containsKey(y));
	}

	public ArrayList<Tile> explode()
	{
		ArrayList<Tile> tiles = new ArrayList<Tile>();

		for (Hashtable<Integer, Tile> hash : super.values())
		{
			for (Tile tile : hash.values())
			{
				tiles.add(tile);
			}
		}

		return tiles;
	}
}