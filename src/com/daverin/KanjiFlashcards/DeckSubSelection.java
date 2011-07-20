package com.daverin.KanjiFlashcards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class DeckSubSelection extends Activity {
  public static class Param implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public BitSet selected_subsets;
    public int cards_per_subset;
    public int set_size;
  }
  public static final Extra<Param> PARAM = Extra.forSerializable();
  
  public static final Extra<BitSet> RETURN_SELECTED_SUBSETS
      = Extra.forSerializable();
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.configure_decks);
    LinearLayout deck_subselection_layout = (LinearLayout)findViewById(R.id.configure_layout);

    param_ = PARAM.get(getIntent());
    
    subselection_buttons_ = new ArrayList<ToggleButton>();
    deck_subselection_layout.removeAllViews();
    int start_number = 0;
    while (start_number < param_.set_size) {
      ToggleButton next_selection_button = new ToggleButton(this);
      next_selection_button.setLayoutParams(
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.FILL_PARENT,
              LinearLayout.LayoutParams.WRAP_CONTENT));
      int end_location = Math.min(start_number + param_.cards_per_subset, param_.set_size);
      next_selection_button.setTextOn(Integer.toString(start_number + 1) + " - " + Integer.toString(end_location));
      next_selection_button.setTextOff(Integer.toString(start_number + 1) + " - " + Integer.toString(end_location));
      next_selection_button.setChecked(param_.selected_subsets.get(start_number / param_.cards_per_subset));
      subselection_buttons_.add(next_selection_button);
      deck_subselection_layout.addView(next_selection_button);
      start_number += param_.cards_per_subset;
    }
    
    Button okay_button = new Button(this);
    okay_button.setText("okay");
    okay_button.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
    okay_button.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          BitSet return_value = new BitSet();
          for (int i = 0; i < subselection_buttons_.size(); ++i) {
            return_value.set(i, subselection_buttons_.get(i).isChecked());
          }
          Intent i = new Intent();
          i.putExtras(getIntent().getExtras());
          RETURN_SELECTED_SUBSETS.put(i, return_value);
          setResult(RESULT_OK, i);
          finish();
        }
    });
    deck_subselection_layout.addView(okay_button);
  }
  
  private Param param_;
  private List<ToggleButton> subselection_buttons_;
}
