package com.fuchs.maps;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;

import com.fuchs.maps.helpers.PointD;
import com.fuchs.poi.POIProvider;

public class MyPreferences extends PreferenceActivity
{
	Context this_;

	private static final class Pref
	{
		public final static String Tile_Size = "tileSize";
		public final static String Track_Step = "trackStep";
		public final static String Map_Folder = "mapFolder";
		public final static String Map_File = "mapFile";

		public final static String Export_Tracks = "export_tracks";
		public final static String Import_Tracks = "import_tracks";

		public final static String Export_POI = "export_poi";
		public final static String Import_POI = "import_poi";

		public final static String Dest_X = "dest_x";
		public final static String Dest_Y = "dest_y";
	}

	private static final class ViewPref
	{
		public final static String View_Settings = "View_Settings";

		public final static String SEEK_LON = "seek_lon";
		public final static String SEEK_LAT = "seek_lat";
		public final static String ZOOM = "zoom";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this_ = this;

		addPreferencesFromResource(R.xml.prefs);

		ListPreference prefTileSize = (ListPreference) findPreference(Pref.Tile_Size);
		prefTileSize.setSummary(prefTileSize.getValue());
		prefTileSize.setOnPreferenceChangeListener(prefOnChangeListener);

		ListPreference prefTrackStep = (ListPreference) findPreference(Pref.Track_Step);
		prefTrackStep.setSummary(prefTrackStep.getValue());
		prefTrackStep.setOnPreferenceChangeListener(prefOnChangeListener);

		Preference mapFilePref = findPreference(Pref.Map_File);
		mapFilePref.setOnPreferenceClickListener(mapFilePrefOnClick);
		mapFilePref.setSummary(getMapFile(this));

		EditTextPreference mapFolderPref = (EditTextPreference) findPreference(Pref.Map_Folder);
		mapFolderPref.setOnPreferenceChangeListener(prefOnChangeListener);
		mapFolderPref.setSummary(getMapFolder(this));
	}

	OnPreferenceChangeListener prefOnChangeListener = new OnPreferenceChangeListener()
	{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue)
		{
			preference.setSummary(newValue.toString());
			return true;
		}
	};

	OnPreferenceClickListener mapFilePrefOnClick = new OnPreferenceClickListener()
	{
		@Override
		public boolean onPreferenceClick(Preference preference)
		{
			Intent intent = new Intent(this_, SelectMapActivity.class);
			startActivity(intent);
			return true;
		}
	};

	// /////////////

	private static SharedPreferences getPref(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	private static SharedPreferences getViewPref(Context context)
	{
		return context.getSharedPreferences(ViewPref.View_Settings, MODE_PRIVATE);
	}

	private static String getAppName(Context context)
	{
		final PackageManager pm = context.getApplicationContext().getPackageManager();
		ApplicationInfo ai;
		try
		{
			ai = pm.getApplicationInfo(context.getPackageName(), 0);
		}
		catch (final NameNotFoundException e)
		{
			ai = null;
		}

		final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

		return applicationName;
	}

	public static int getTrackStep(Context context)
	{
		String step = getPref(context).getString(Pref.Track_Step, "1");
		return Integer.parseInt(step);
	}

	public static int getTileSize(Context context)
	{
		String size = getPref(context).getString(Pref.Tile_Size, "256");
		return Integer.parseInt(size);
	}

	protected static String getMapFolder(Context context)
	{
		return getPref(context).getString(Pref.Map_Folder, Environment.getExternalStorageDirectory() + "/" + getAppName(context) + "/");
	}

	protected static String getMapFile(Context context)
	{
		return getPref(context).getString(Pref.Map_File, null);
	}

	protected static String getFullMapFile(Context context)
	{
		return getMapFolder(context) + getMapFile(context);
	}

	protected static void setMapFile(Context context, String file)
	{
		SharedPreferences.Editor editor = getPref(context).edit();
		editor.putString(Pref.Map_File, file);
		editor.commit();
	}

	// ///////

	protected static double getViewSeekLon(Context context)
	{
		String value = getViewPref(context).getString(ViewPref.SEEK_LON, "0");
		return Double.parseDouble(value);
	}

	protected static double getViewSeekLat(Context context)
	{
		String value = getViewPref(context).getString(ViewPref.SEEK_LAT, "0");
		return Double.parseDouble(value);
	}

	protected static int getViewZoom(Context context)
	{
		return getViewPref(context).getInt(ViewPref.ZOOM, 0);
	}

	protected static void setViewSeek(Context context, PointD seekLocation)
	{
		SharedPreferences pref = getViewPref(context);
		SharedPreferences.Editor editor = pref.edit();

		editor.putString(ViewPref.SEEK_LON, Double.toString(seekLocation.x));
		editor.putString(ViewPref.SEEK_LAT, Double.toString(seekLocation.y));

		editor.commit();
	}

	protected static void setViewSeek(Context context, PointD seekLocation, int zoom)
	{
		SharedPreferences pref = getViewPref(context);
		SharedPreferences.Editor editor = pref.edit();

		editor.putString(ViewPref.SEEK_LON, Double.toString(seekLocation.x));
		editor.putString(ViewPref.SEEK_LAT, Double.toString(seekLocation.y));
		editor.putInt(ViewPref.ZOOM, zoom);

		editor.commit();
	}

	// ////

	public static void setDestination(Context context, PointD dest)
	{
		SharedPreferences.Editor editor = getPref(context).edit();

		if (dest != null)
		{
			editor.putString(Pref.Dest_X, Double.toString(dest.x));
			editor.putString(Pref.Dest_Y, Double.toString(dest.y));
		}
		else
		{
			editor.remove(Pref.Dest_X);
			editor.remove(Pref.Dest_Y);
		}

		editor.commit();
	}

	public static PointD getDestination(Context context)
	{
		SharedPreferences pref = getPref(context);
		String strX = pref.getString(Pref.Dest_X, "");
		String strY = pref.getString(Pref.Dest_Y, "");

		if (strX == "" || strY == "") return null;

		return new PointD(Double.parseDouble(strX), Double.parseDouble(strY));
	}
}
