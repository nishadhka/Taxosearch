package com.TSQG.map.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

// http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html

public class CompassSensor implements SensorEventListener
{
	private SensorManager mSensorManager;
	Sensor accelerometer;
	Sensor magnetometer;

	Float azimut;
	Handler handler;

	public CompassSensor(Context context, Handler handler)
	{
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		this.handler = handler;
	}

	public void resume()
	{
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void pause()
	{
		mSensorManager.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	float[] mGravity;
	float[] mGeomagnetic;
	Message msg = new Message();

	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null)
		{
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success)
			{
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimut = orientation[0]; // orientation contains: azimut,
											// pitch
											// and roll
			}
		}

		msg = new Message();
		msg.obj = azimut;
		if (handler != null) handler.sendMessage(msg);

	}
}