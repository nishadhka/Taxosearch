/**
 * 
 */
package com.tsqg.quiz.main;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tsqg.game.R;
import com.tsqg.quiz.Constants;
import com.tsqg.quiz.GamePlay;
import com.tsqg.quiz.Question;
import com.tsqg.quiz.util.Utility;

/**
 * @author robert.hinds
 *
 */
public class QuestionActivity3 extends Activity {

	private Question currentQ;
	private GamePlay currentGame;
	Chronometer chron;
	public String srt;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question);
        /**
         * Configure current game and get question
         */
        currentGame = ((ChuckApplication)getApplication()).getCurrentGame();
        currentQ = currentGame.getNextQuestion();
        
        /**
         * Update the question and answer options..
         */
        setQuestions();
        Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		String chronoText = extras.getString("longi1");
        //Toast.makeText(this, "Elapsed milliseconds: " + chronoText, Toast.LENGTH_SHORT).show();

        final Chronometer chron = (Chronometer) this.findViewById(R.id.chronometer);
        
        /*SharedPreferences myPrefs = this.getSharedPreferences("que1str", MODE_WORLD_READABLE);
        String chronoText = myPrefs.getString("que1strK","q");*/
                
        int stoppedMilliseconds = 0;

       //String chronoText = mChr.getText().toString();
        String array[] = chronoText.split(":");
        if (array.length == 2) {
          stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
              + Integer.parseInt(array[1]) * 1000;
        } else if (array.length == 3) {
          stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000 
              + Integer.parseInt(array[1]) * 60 * 1000
              + Integer.parseInt(array[2]) * 1000;
       }

        chron.setBase(SystemClock.elapsedRealtime() - stoppedMilliseconds);
        chron.start();
        
        Button nextBtn = (Button) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
		     public void onClick(View v) {
			 if (!checkAnswer()) return;	
			 String srten2 = chron.getText().toString();
			 Intent i = new Intent(QuestionActivity3.this, QuestionActivity4.class);
				i.putExtra("longi2",srten2);
				startActivity(i);
				finish();
		     }
		 });
      }
     

          
   
   
	/**
	 * Method to set the text for the question and answers from the current games
	 * current question
	 */
	private void setQuestions() {
		//set the question text from current question
		String question = Utility.capitalise(currentQ.getQuestion()) + "?";
        TextView qText = (TextView) findViewById(R.id.question);
        qText.setText(question);
        
        //set the available options
        List<String> answers = currentQ.getQuestionOptions();
        TextView option1 = (TextView) findViewById(R.id.answer1);
        option1.setText(Utility.capitalise(answers.get(0)));
        
        TextView option2 = (TextView) findViewById(R.id.answer2);
        option2.setText(Utility.capitalise(answers.get(1)));
        
        TextView option3 = (TextView) findViewById(R.id.answer3);
        option3.setText(Utility.capitalise(answers.get(2)));
        
        TextView option4 = (TextView) findViewById(R.id.answer4);
        option4.setText(Utility.capitalise(answers.get(3)));
	}

	/*@Override
	public void onClick(View arg0) 
	
	{
		
		*//**
		 * validate a checkbox has been selected
		 *//*
		
		if (!checkAnswer()) return;

		
		*//**
		 * check if end of game
		 *//*
		
		else{
			//String srt1 = chron.getText().toString();
			Intent i = new Intent(QuestionActivity3.this, EndgameActivity.class);
			//i.putExtra("longi",srt1);
			startActivity(i);
			finish();
		}
	}*/
	
	
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


	/**
	 * Check if a checkbox has been selected, and if it
	 * has then check if its correct and update gamescore
	 */
	private boolean checkAnswer() {
		String answer = getSelectedAnswer();
		if (answer==null){
			//Log.d("Questions", "No Checkbox selection made - returning");
			return false;
		}
		else {
			//Log.d("Questions", "Valid Checkbox selection made - check if correct");
			if (currentQ.getAnswer().equalsIgnoreCase(answer))
			{
				//Log.d("Questions", "Correct Answer!");
				currentGame.incrementRightAnswers();
			}
			else{
				//Log.d("Questions", "Incorrect Answer!");
				currentGame.incrementWrongAnswers();
			}
			return true;
		}
	}


	/**
	 * 
	 */
	private String getSelectedAnswer() {
		RadioButton c1 = (RadioButton)findViewById(R.id.answer1);
		RadioButton c2 = (RadioButton)findViewById(R.id.answer2);
		RadioButton c3 = (RadioButton)findViewById(R.id.answer3);
		RadioButton c4 = (RadioButton)findViewById(R.id.answer4);
		if (c1.isChecked())
		{
			return c1.getText().toString();
		}
		if (c2.isChecked())
		{
			return c2.getText().toString();
		}
		if (c3.isChecked())
		{
			return c3.getText().toString();
		}
		if (c4.isChecked())
		{
			return c4.getText().toString();
		}
		
		return null;
	}
	
}
