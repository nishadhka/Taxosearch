package com.TSQG.map.poi;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;

import com.TSQG.map.database.PoiDB;

public class POIProvider
{
	public static POI getPOI(Context context, long id)
	{
		PoiDB poiDB = new PoiDB(context);
		poiDB.openDataBase();
		POI point = poiDB.getPOI(id);
		poiDB.close();

		return point;
	}

	public static ArrayList<POI> getAllPoints(Context context, boolean onlyVisible)
	{
		PoiDB poiDB = new PoiDB(context);
		poiDB.openDataBase();
		ArrayList<POI> points = poiDB.getPOIList(onlyVisible);
		poiDB.close();

		return points;
	}

	

	
}
