/**
 * 
 */
package com.tsqg.quiz.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tsqg.game.R;
import com.tsqg.maps.FMapsActivity;
import com.tsqg.quiz.Constants;
import com.tsqg.quiz.GamePlay;
import com.tsqg.quiz.Helper;

/**
 * @author robert.hinds
 *
 */
public class EndgameActivity extends Activity implements OnClickListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.endgame);
		GamePlay currentGame = ((ChuckApplication)getApplication()).getCurrentGame();
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		String chronoText = extras.getString("longi3");
		String result = "You Got " + currentGame.getRight() + "/" + currentGame.getNumRounds() + " and took " + chronoText + " Seconds/Minutes to complete the quiz " ;
		String comment = Helper.getResultComment(currentGame.getRight(), currentGame.getNumRounds(), getDifficultySettings());
		
		
		TextView results = (TextView)findViewById(R.id.endgameResult);
		results.setText(result + comment);
		
		int image = Helper.getResultImage(currentGame.getRight(), currentGame.getNumRounds(), getDifficultySettings());
		ImageView resultImage = (ImageView)findViewById(R.id.resultPage);
		resultImage.setImageResource(image);
		
		//handle button actions
		Button finishBtn = (Button) findViewById(R.id.finishBtn);
		finishBtn.setOnClickListener(this);
		Button answerBtn = (Button) findViewById(R.id.answerBtn);
		answerBtn.setOnClickListener(this);
		/*Button searchBtn = (Button) findViewById(R.id.searchBtn);
		answerBtn.setOnClickListener(this);
*/		
	}
	
	
	/**
	 * Method to return the difficulty settings
	 * @return
	 */
	private int getDifficultySettings() {
		SharedPreferences settings = getSharedPreferences(Constants.SETTINGS, 0);
		int diff = settings.getInt(Constants.DIFFICULTY, 2);
		return diff;
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 * 
	 * This method is to override the back button on the phone
	 * to prevent users from navigating back in to the quiz
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
		case KeyEvent.KEYCODE_BACK :
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.finishBtn :
			Intent i = new Intent(EndgameActivity.this, TSQ_wel.class);
			startActivity(i);
			break;
			
		case R.id.answerBtn :
			Intent ip = new Intent(this, AnswersActivity.class);
			startActivityForResult(ip, Constants.PLAYBUTTON);
			break;
			
		/*case R.id.searchBtn :
			Intent intent = new Intent(this,FMapsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);*/
		}
	}

}
