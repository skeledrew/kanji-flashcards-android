package com.daverin.KanjiFlashcards;

import java.io.Serializable;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class QuizResults extends Activity {
  public static class Param implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public float quiz_percent;
    public String quiz_count_string;
  }
  public static final Extra<Param> PARAM = Extra.forSerializable();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    param_ = PARAM.get(getIntent());
    
    setContentView(R.layout.quiz_results);
    ((TextView)findViewById(R.id.quiz_percent)).setText(String.format("%1$.1f", param_.quiz_percent) + "%");
    ((TextView)findViewById(R.id.quiz_count)).setText(param_.quiz_count_string);
       
    ((TextView)findViewById(R.id.quiz_okay_button)).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });
  }
  
  private Param param_;
}
