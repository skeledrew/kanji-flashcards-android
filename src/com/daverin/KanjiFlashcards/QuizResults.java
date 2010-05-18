package com.daverin.KanjiFlashcards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class QuizResults extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.quiz_results);
        ((TextView)findViewById(R.id.quiz_percent)).setText(String.format("%1$.1f", Global.current_deck_.quizPercent()) + "%");
        ((TextView)findViewById(R.id.quiz_count)).setText(Global.current_deck_.quizCountString());
       
        ((TextView)findViewById(R.id.quiz_okay_button)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent();
				setResult(RESULT_OK, i);
				finish();
			}
        });
    }
}
