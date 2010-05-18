package com.daverin.KanjiFlashcards;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class DeckSubSelection extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.configure_decks);
        LinearLayout deck_subselection_layout = (LinearLayout)findViewById(R.id.configure_layout);
        
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
        	finish();
        }
        
        deck_index_ = extras.getInt("deck_index");
        if (deck_index_ == null) {
        	deck_index_ = 0;
        }
        
        subselection_buttons_.clear();
        deck_subselection_layout.removeAllViews();
        int start_number = 0;
        while (start_number < Global.decks_.elementAt(deck_index_).numCards()) {
        	ToggleButton next_selection_button = new ToggleButton(this);
        	next_selection_button.setLayoutParams(
        			new LinearLayout.LayoutParams(
        					LinearLayout.LayoutParams.FILL_PARENT,
        					LinearLayout.LayoutParams.WRAP_CONTENT));
        	int end_location = Math.min(start_number + 20, Global.decks_.elementAt(deck_index_).numCards());
        	next_selection_button.setTextOn(Integer.toString(start_number + 1) + " - " + Integer.toString(end_location));
        	next_selection_button.setTextOff(Integer.toString(start_number + 1) + " - " + Integer.toString(end_location));
        	next_selection_button.setChecked(Global.deck_sub_selections_.elementAt(deck_index_).elementAt(start_number / 20));
        	subselection_buttons_.add(next_selection_button);
        	deck_subselection_layout.addView(next_selection_button);
        	start_number += 20;
        }
        
        Button okay_button = new Button(this);
        okay_button.setText("okay");
        okay_button.setLayoutParams(
    			new LinearLayout.LayoutParams(
    					LinearLayout.LayoutParams.FILL_PARENT,
    					LinearLayout.LayoutParams.WRAP_CONTENT));
        okay_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				for (int i = 0; i < subselection_buttons_.size(); ++i) {
					if (subselection_buttons_.elementAt(i).isChecked()) {
						Global.deck_sub_selections_.elementAt(deck_index_).setElementAt(true, i);
					} else {
						Global.deck_sub_selections_.elementAt(deck_index_).setElementAt(false, i);
					}
				}
				Intent i = new Intent();
        		setResult(RESULT_OK, i);
        		finish();
			}
        });
        deck_subselection_layout.addView(okay_button);
    }
	
	Vector<ToggleButton> subselection_buttons_ = new Vector<ToggleButton>();
	Integer deck_index_;
}
