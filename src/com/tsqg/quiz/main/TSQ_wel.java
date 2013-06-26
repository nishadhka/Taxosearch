package com.tsqg.quiz.main;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.tsqg.game.R;
import com.tsqg.quiz.Constants;
import com.tsqg.quiz.GamePlay;
import com.tsqg.quiz.Question;

public class TSQ_wel extends Activity implements OnClickListener{

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		//////////////////////////////////////////////////////////////////////
		//////// GAME MENU  /////////////////////////////////////////////////
		Button playBtn = (Button) findViewById(R.id.playBtn);
		playBtn.setOnClickListener(this);
		Button settingsBtn = (Button) findViewById(R.id.settingsBtn);
		settingsBtn.setOnClickListener(this);
		Button rulesBtn = (Button) findViewById(R.id.rulesBtn);
		rulesBtn.setOnClickListener(this);
		Button exitBtn = (Button) findViewById(R.id.exitBtn);
		exitBtn.setOnClickListener(this);
	}


	/**
	 * Listener for game menu
	 */
	@Override
	public void onClick(View v) {
		Intent i;
		
		switch (v.getId()){
		case R.id.playBtn :
			
			i = new Intent(this, com.tsqg.maps.FMapsActivity.class);
			startActivityForResult(i, Constants.PLAYBUTTON);
			break;
			
		case R.id.rulesBtn :
			i = new Intent(this, RulesActivity.class);
			startActivityForResult(i, Constants.RULESBUTTON);
			break;
			
		case R.id.settingsBtn :
			i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, Constants.SETTINGSBUTTON);
			break;
			
		case R.id.exitBtn :
			finish();
			break;
		}
}
}