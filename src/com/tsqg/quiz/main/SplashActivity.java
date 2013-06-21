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
import android.widget.ImageView;
import android.widget.Toast;

import com.tsqg.game.R;
import com.tsqg.quiz.Constants;
import com.tsqg.quiz.GamePlay;
import com.tsqg.quiz.Question;
import com.tsqg.quiz.db.DBHelper;

public class SplashActivity extends Activity implements OnClickListener{

	ImageView leafimg;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.another);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		String value1 = extras.getString("SRT");
		Toast.makeText(this,value1,Toast.LENGTH_LONG).show();
		if(value1.equals ("11")){
			ImageView leafimg= (ImageView)findViewById(R.id.imageView2);
			leafimg.setImageResource(R.drawable.hard_winner);
		} 
		//////////////////////////////////////////////////////////////////////
		//////// GAME MENU  /////////////////////////////////////////////////
		Button playBtn = (Button) findViewById(R.id.button1);
		playBtn.setOnClickListener(this);
		//Intent i = getIntent();
		//ImageView leafimg= (ImageView)findViewById(R.id.imageView2);
		//String txtData = i.getExtras().getString(SRT,"");
		////String quizno = extras.getString("quiz");
		//Toast.makeText(this,txtData,Toast.LENGTH_LONG).show();  
		//String quizno = this.getIntent().getStringExtra("quiz");
		//leafimg = (ImageView)findViewById(R.id.imageView2);
		
		//if(quizno.equals ("11")){
		//leafimg.setImageResource(R.drawable.hard_winner);
		//} 
		
		//ImageView img = new ImageView(this);
		//leafimg.setImageResource(R.drawable.hard_winner);	 
				 
		}
		//}
	

	
	/**
	 * Listener for game menu
	 */
	@Override
	public void onClick(View v) {
		Intent i;
		
		switch (v.getId()){
		case R.id.button1 :
			//once logged in, load the main page
			//Log.d("LOGIN", "User has started the game");
			
			//Get Question set //
			List<Question> questions = getQuestionSetFromDb();

			//Initialise Game with retrieved question set ///
			GamePlay c = new GamePlay();
			c.setQuestions(questions);
			c.setNumRounds(getNumQuestions());
			((ChuckApplication)getApplication()).setCurrentGame(c);  

			//Start Game Now.. //
			i = new Intent(this, QuestionActivity.class);
			startActivityForResult(i, Constants.PLAYBUTTON);
			break;
			
		
		}

	}


	/**
	 * Method that retrieves a random set of questions from
	 * the database for the given difficulty
	 * @return
	 * @throws Error
	 */
	private List<Question> getQuestionSetFromDb() throws Error {
		int diff = getDifficultySettings();
		int numQuestions = getNumQuestions();
		String plantN = getplantNo();
		DBHelper myDbHelper = new DBHelper(this);
		try {
			myDbHelper.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
		try {
			myDbHelper.openDataBase();
		}catch(SQLException sqle){
			throw sqle;
		}
		List<Question> questions = myDbHelper.getQuestionSet(diff, numQuestions, plantN);
		myDbHelper.close();
		return questions;
	}


	/**
	 * Method to return the difficulty settings
	 * @return
	 */
	private int getDifficultySettings() {
		SharedPreferences settings = getSharedPreferences(Constants.SETTINGS, 0);
		int diff = settings.getInt(Constants.DIFFICULTY, Constants.MEDIUM);
		return diff;
	}

	/**
	 * Method to return the number of questions for the game
	 * @return
	 */
	private int getNumQuestions() {
		SharedPreferences settings = getSharedPreferences(Constants.SETTINGS, 0);
		int numRounds = settings.getInt(Constants.NUM_ROUNDS, 3);
		return numRounds;
	}
	
	private String getplantNo() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return null ;
		}
		String userEntry = extras.getString("SRT");
		String plantNo = null;
		if(userEntry.equals ("11")){
		plantNo = "1";
	}
		if(userEntry.equals ("12")){
			plantNo = "2";
		}
		return plantNo;

}
}