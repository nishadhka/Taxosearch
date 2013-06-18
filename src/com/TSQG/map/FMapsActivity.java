package com.TSQG.map;

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

import com.TSQG.map.helpers.PointD;
import com.TSQG.map.poi.POI;
import com.TSQG.map.poi.POIProvider;
import com.TSQG.map.tileManagement.TilesManager;
import com.TSQG.map.tileManagement.TilesProvider;
import com.TSQG.map.util.CompassSensor;
import com.TSQG.map.views.IMapViewEventListener;
import com.TSQG.map.views.MapView;
import com.TSQG.map.views.MapViewLocationListener;
import com.TSQG.map.views.overlays.POIOverlay;
import com.TSQG.map.views.overlays.PathOverlay;
import com.TSQG.map.views.overlays.PositionMarkerOverlay;
import com.TSQG.quizgame.R;


public class FMapsActivity extends Activity implements IMapViewEventListener
{
	final static int REQ_SELECT_POI = 0;
	final static int REQ_SELECT_TRK = 1;

	// // Fields
	MapView mapView;
	ImageView imgSat;
	ImageView imgDestX;
	ImageView imgCompass;
	ImageView imgQues;

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
		//btnOptions = (Button) findViewById(R.id.btnOptions);
		//btnOptions.setOnClickListener(btnOptionsOnClick);
		//btnOptions.setVisibility(View.INVISIBLE);

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
		
		imgQues = (ImageView) findViewById(R.id.imgQuest);
		imgQues.setOnClickListener(new View.OnClickListener() {
			//When you click the button, Alert dialog will be showed
	        public void onClick(View v) {
	       /* Alert Dialog Code Start*/ 	
	        	AlertDialog.Builder alert = new AlertDialog.Builder(this_);
	        	alert.setTitle("Alert Dialog With EditText"); //Set Alert dialog title here
	        	alert.setMessage("Enter Your Name Here"); //Message here

	            // Set an EditText view to get user input 
	            final EditText input = new EditText(this_);
	            alert.setView(input);

	        	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int whichButton) {
	        	 //You will get as string input data in this variable.
	        	 // here we convert the input to a string and show in a toast.
	        		
	        	 String srt = input.getEditableText().toString();
	        	 if(srt.equals("11")){
	        		    Intent i = new Intent("com.fuchs.maps.TSQ_wel");
	        		        startActivity(i);
	        		}
	        	 
	        	 //if(srt.equals("12")){
	        	//	 Toast.makeText(this_,srt,Toast.LENGTH_LONG).show();  
	        	//	}
	        	 //if(srt.equals("13")){
	        		// Toast.makeText(this_,"high this is nis",Toast.LENGTH_LONG).show();  
	        		//}
	        	 else {
	        		Toast.makeText(this_,"enter correct number",Toast.LENGTH_LONG).show();   
	        		}
	        	 //Toast.makeText(this_,srt,Toast.LENGTH_LONG).show();        		
	        	} // End of onClick(DialogInterface dialog, int whichButton)
	        }); //End of alert.setPositiveButton
	        	alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
	        	  public void onClick(DialogInterface dialog, int whichButton) {
	        	    // Canceled.
	        		  dialog.cancel();
	        	  }
	        }); //End of alert.setNegativeButton
	        	AlertDialog alertDialog = alert.create();
	        	alertDialog.show();
	       /* Alert Dialog Code End*/        
	     }// End of onClick(View v)
	  }); //button.setOnClickListener
	
		
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

	// Location Options

	@Override
	public boolean onScroll(double lon, double lat, float x, float y)
	{
		//btnOptions.setVisibility(View.GONE);
		return false;
	}
}