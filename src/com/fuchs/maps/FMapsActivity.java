package com.fuchs.maps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.fuchs.maps.helpers.PointD;
import com.fuchs.maps.tileManagement.TilesManager;
import com.fuchs.maps.tileManagement.TilesProvider;
import com.fuchs.maps.views.IMapViewEventListener;
import com.fuchs.maps.views.MapView;
import com.fuchs.maps.views.MapViewLocationListener;
import com.fuchs.maps.views.overlays.POIOverlay;
import com.fuchs.maps.views.overlays.PathOverlay;
import com.fuchs.maps.views.overlays.PositionMarkerOverlay;
import com.fuchs.poi.POI;
import com.fuchs.poi.POIProvider;
import com.fuchs.util.CompassSensor;

public class FMapsActivity extends Activity implements IMapViewEventListener
{
	final static int REQ_SELECT_POI = 0;
	final static int REQ_SELECT_TRK = 1;

	// // Fields
	MapView mapView;
	ImageView imgSat;
	ImageView imgDestX;
	ImageView imgCompass;

	PositionMarkerOverlay positionMarkerOverlay;
	POIOverlay poiOverlay;
	PathOverlay pathOverlay;

	MapViewLocationListener locationListener;

	Location savedGpsLocation;
	TilesProvider tilesProvider;

	Button btnOptions;

	long backKeyTime = -1;
	CompassSensor compass;

	Context this_;

	Display display;

	public FMapsActivity()
	{
		this_ = this;
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()));
	}

	Handler compassHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			Float azimut = (Float) msg.obj;

			if (azimut != null)
			{
				Matrix matrix = new Matrix();

				// Dirty way of getting the rotation
				int rot = display.getRotation() * 90;

				imgCompass.setScaleType(ScaleType.MATRIX); // required
				matrix.postRotate((float) -azimut * 360 / (2 * 3.14159f) - rot, imgCompass.getWidth() / 2, imgCompass.getHeight() / 2);
				imgCompass.setImageMatrix(matrix);
			}
		};
	};

	Handler gpsStateChangeHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			int status = msg.what;
			if (status == LocationProvider.AVAILABLE)
			{
				imgSat.setBackgroundResource(R.drawable.gps_on);
			}
			else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
			{
				imgSat.setBackgroundResource(R.anim.gps_ani);
				((AnimationDrawable) imgSat.getBackground()).start();
			}
			else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
			{
				imgSat.setBackgroundResource(0);
			}
		}
	};

	// // Activity life cycle

	LocationManager getLocationManager()
	{
		return (LocationManager) getSystemService(LOCATION_SERVICE);
	}

	public static AnimationDrawable getAnimation(View v)
	{
		return ((AnimationDrawable) v.getBackground());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		compass = new CompassSensor(this, compassHandler);
	}

	@Override
	protected void onResume()
	{
		setContentView(R.layout.main);
		super.onResume();

		display = getWindowManager().getDefaultDisplay();

		initViews();
		StateManager.restoreMapViewPreferences(this, mapView);

		initOverlays();

		if (savedGpsLocation != null) mapView.setGpsLocation(savedGpsLocation);

		LocationManager locationManager = getLocationManager();
		locationListener = new MapViewLocationListener(mapView, gpsStateChangeHandler);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

		compass.resume();
	}

	@Override
	protected void onPause()
	{
		compass.pause();

		StateManager.saveMapViewPreferences(this, mapView);

		locationListener.stop();

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.removeUpdates(locationListener);

		if (tilesProvider != null)
		{
			tilesProvider.close();
			tilesProvider.clear();
		}

		mapView = null;

		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		if (resultCode != RESULT_OK) return;

		if (requestCode == REQ_SELECT_POI)
		{
			long id = data.getLongExtra("id", -1);
			POI point = POIProvider.getPOI(this, id);
			MyPreferences.setViewSeek(this, point.getPointD());
		}
		else if (requestCode == REQ_SELECT_TRK)
		

		super.onActivityResult(requestCode, resultCode, data);
	}

	// // Activity state manipulation

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		StateManager.onSaveInstanceState(outState, mapView);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		savedGpsLocation = StateManager.onRestoreInstanceState(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	// // Components initialization

	void initViews()
	{
		try
		{
			String path = Environment.getExternalStorageDirectory() + "/mapapp/world.sqlitedb";
			tilesProvider = new TilesProvider(path);
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Error creating tiles provider", Toast.LENGTH_SHORT).show();
		}

		mapView = new MapView(this, display.getWidth(), display.getHeight(), tilesProvider, MyPreferences.getTileSize(this));
		mapView.setEventsListener(this);

		((RelativeLayout) findViewById(R.id.frame)).addView(mapView, 0);
		btnOptions = (Button) findViewById(R.id.btnOptions);
		btnOptions.setOnClickListener(btnOptionsOnClick);
		btnOptions.setVisibility(View.INVISIBLE);

		imgCompass = (ImageView) findViewById(R.id.imgCompass);

		imgDestX = (ImageView) findViewById(R.id.imgDestX);
		imgDestX.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				MyPreferences.setDestination(this_, null);
				pathOverlay.setDestenation(null);
				imgDestX.setVisibility(View.INVISIBLE);
				mapView.invalidate();
			}
		});
		if (StateManager.destinationExists(this)) imgDestX.setVisibility(View.VISIBLE);
		else
			imgDestX.setVisibility(View.INVISIBLE);

		imgSat = (ImageView) findViewById(R.id.imgSat);
		boolean gpsEnabled = getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (gpsEnabled)
		{
			imgSat.post(new Runnable()
			{
				@Override
				public void run()
				{
					imgSat.setBackgroundResource(R.anim.gps_ani);
					((AnimationDrawable) imgSat.getBackground()).start();
				}
			});
		}

		((RadioButton) findViewById(R.id.radioFollow)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mapView.followMarker();
			}
		});

		ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom);
		zoomControls.setOnZoomInClickListener(onZoomInClick);
		zoomControls.setOnZoomOutClickListener(onZoomOutClick);

		mapView.refresh();
	}

	void initOverlays()
	{
		Bitmap posMarker = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
		Bitmap poiMarker = BitmapFactory.decodeResource(getResources(), R.drawable.poi);
		Bitmap pathMarker = BitmapFactory.decodeResource(getResources(), R.drawable.dest);
		TilesManager tilesManager = mapView.getTilesManager();

		
		positionMarkerOverlay = new PositionMarkerOverlay(tilesManager, posMarker);
		poiOverlay = new POIOverlay(tilesManager, this, poiMarker);
		pathOverlay = new PathOverlay(tilesManager, pathMarker);

		
		mapView.addOverlay(poiOverlay);
		mapView.addOverlay(positionMarkerOverlay);
		mapView.addOverlay(pathOverlay);

		StateManager.restorePOIOverlayState(this, poiOverlay);
		StateManager.restorePathOverlayState(this, pathOverlay);
	}

	// // Options menu

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.menu.maps:
				startActivity(new Intent(this, SelectMapActivity.class));
				return true;
			case R.menu.prefs:
				startActivity(new Intent(this, MyPreferences.class));
				return true;
			case R.menu.exit:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// // Events handling

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_M)
		{
			mapView.setGpsLocation(76.786977056886, 11.092323819616, 863.7999877929688, 182);
			mapView.invalidate();

			return false;
		}
		else if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (backKeyTime == -1)
			{
				backKeyTime = System.currentTimeMillis();
				Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
			}
			else
			{
				long diff = System.currentTimeMillis() - backKeyTime;
				if (diff < 800)
				{
					finish();
				}
				else
				{
					backKeyTime = System.currentTimeMillis();
					Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
				}
			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	View.OnClickListener onZoomInClick = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			mapView.zoomIn();
		}
	};

	View.OnClickListener onZoomOutClick = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			mapView.zoomOut();
		}
	};

	// // Location Options

	@Override
	protected Dialog onCreateDialog(int id, Bundle args)
	{
		final Dialog d = new Dialog(this);
		d.setTitle("Location options");
		d.setContentView(R.layout.map_dialog);

		
		OnClickListener btnSetDestOnClick = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				PointD p = (PointD) btnOptions.getTag();
				pathOverlay.setDestenation(p);
				MyPreferences.setDestination(this_, p);
				imgDestX.setVisibility(View.VISIBLE);
				mapView.invalidate();

				d.dismiss();
			}
		};

		OnClickListener btnShareLocationOnClick = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

				PointD p = (PointD) btnOptions.getTag();
				String str = p.y + " ," + p.x;

				intent.putExtra(Intent.EXTRA_SUBJECT, str);
				intent.putExtra(Intent.EXTRA_TEXT, str);

				startActivity(Intent.createChooser(intent, "Share coordinates"));

				d.dismiss();
			}
		};

		
		d.findViewById(R.dialog.btnSetDest).setOnClickListener(btnSetDestOnClick);
		d.findViewById(R.dialog.btnShare).setOnClickListener(btnShareLocationOnClick);

		return d;
	}

	OnClickListener btnOptionsOnClick = new OnClickListener()
	{
		@Override
		public void onClick(final View v)
		{
			showDialog(0);
			btnOptions.setVisibility(View.INVISIBLE);
		}
	};

	Handler hideAddPOiHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			if (msg.what == -1)
			{
				btnOptions.setVisibility(View.INVISIBLE);
			}
			else
			{
				hideAddPOiHandler.sendEmptyMessageDelayed(-1, 3000);
			}
		};
	};

	@Override
	public boolean onLongPress(double lon, double lat, float x, float y)
	{
		RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int) x;
		params.topMargin = (int) y;
		btnOptions.setLayoutParams(params);

		btnOptions.setVisibility(View.VISIBLE);
		btnOptions.setTag(new PointD(lon, lat));

		hideAddPOiHandler.removeMessages(-1);
		hideAddPOiHandler.sendEmptyMessage(0);

		return false;
	}

	@Override
	public boolean onScroll(double lon, double lat, float x, float y)
	{
		btnOptions.setVisibility(View.GONE);
		return false;
	}
}