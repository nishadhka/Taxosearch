package com.tsqg.map.database;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

import com.tsqg.map.database.helpers.DatabaseHelper;
import com.tsqg.map.poi.POI;

public class PoiDB extends DatabaseHelper
	{
		public static final int I_ID = 0;
		public static final int I_NAME = 1;
		public static final int I_LON = 2;
		public static final int I_LAT = 3;
		public static final int I_VISIBLE = 4;

		public static final String TABLE_NAME = "POI";

		public static final String K_ID = "_id";
		public static final String K_NAME = "name";
		public static final String K_LON = "lon";
		public static final String K_LAT = "lat";
		public static final String K_VISIBLE = "visible";

		//@Override
		//prlotected void makeSchema()
		//{
			//DBColumn[] cols = new DBColumn[5];

			//cols[I_ID] = new DBColumn(K_ID, DBColumn.DataType.INTEGER, new DBFieldFlags(false, true, true));
			//cols[I_NAME] = new DBColumn(K_NAME, DBColumn.DataType.TEXT, null);
			//cols[I_LON] = new DBColumn(K_LON, DBColumn.DataType.TEXT, null);
			//cols[I_LAT] = new DBColumn(K_LAT, DBColumn.DataType.TEXT, null);
			//cols[I_VISIBLE] = new DBColumn(K_VISIBLE, DBColumn.DataType.INTEGER, null);

			//schema = new DBSchema(TABLE_NAME, cols);
		

		public PoiDB(Context context)
		{
			super(context);
		}

		
		public Cursor selectByID(long id)
		{
			return myDataBase.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + K_ID + " = " + id, null);
		}

		public Cursor getAll()
		{
			return myDataBase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		}

		public POI getPOI(long id)
		{
			Cursor cursor = this.selectByID(id);
			if (cursor.moveToFirst())
			{
				int _id = cursor.getInt(I_ID);
				String name = cursor.getString(I_NAME);
				double lon = cursor.getDouble(I_LON);
				double lat = cursor.getDouble(I_LAT);

				POI poi = new POI(_id, name, lon, lat);
				return poi;
			}

			return null;
		}

		public ArrayList<POI> getPOIList()
		{
			return getPOIList(false);
		}

		public ArrayList<POI> getPOIList(boolean onlyVisible)
		{
			ArrayList<POI> points = new ArrayList<POI>();

			Cursor cursor = this.getAll();
			if (cursor.moveToFirst())
			{
				do
				{
					int id = cursor.getInt(I_ID);
					String name = cursor.getString(I_NAME);
					double lon = cursor.getDouble(I_LON);
					double lat = cursor.getDouble(I_LAT);
					boolean visible = (cursor.getInt(I_VISIBLE) == 1 ? true : false);

					if (!onlyVisible || (onlyVisible && visible))
					{
						POI poi = new POI(id, name, lon, lat, visible);
						points.add(poi);
					}
				}
				while (cursor.moveToNext());
			}

			return points;
		}
	}
