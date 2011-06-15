package com.daverin.KanjiFlashcards;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class ConfigureDecks extends Activity {

  public static final int SET_SUB_SELECTION = 1;

  public class DeckLongClickListener implements View.OnLongClickListener {

    public DeckLongClickListener(int deck_index) {
      deck_index_ = deck_index;
    }
    public boolean onLongClick(View v) {
      Intent i = new Intent(v.getContext(), DeckSubSelection.class);
      i.putExtra("deck_index", deck_index_);
      startActivityForResult(i, SET_SUB_SELECTION);
      return true;
    }

    int deck_index_;

  }

  public class DeckClickListener implements View.OnClickListener {

    public DeckClickListener(int deck_index) {
      deck_index_ = deck_index;
    }
    public void onClick(View v) {
      for (int i = 0; i < Global.deck_sub_selections_.get(deck_index_).size(); ++i) {
        Global.deck_sub_selections_.get(deck_index_).set(i, deck_buttons_.elementAt(deck_index_).isChecked());
      }
    }

    int deck_index_;

  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
        
    // The interface is designed specifically for japanese kanji character flash cards.
    setContentView(R.layout.configure_decks);
    LinearLayout deck_choice_layout = (LinearLayout)findViewById(R.id.configure_layout);
        
    deck_buttons_.clear();
    deck_choice_layout.removeAllViews();
    for (int i = 0; i < Global.decks_.size(); ++i) {
      ToggleButton next_deck_button = new ToggleButton(this);
      next_deck_button.setLayoutParams(
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.FILL_PARENT,
              LinearLayout.LayoutParams.WRAP_CONTENT));
      next_deck_button.setTextOn(Global.decks_.get(i).name());
      next_deck_button.setTextOff(Global.decks_.get(i).name());
      next_deck_button.setOnLongClickListener(new DeckLongClickListener(i));
      next_deck_button.setOnClickListener(new DeckClickListener(i));
      deck_buttons_.add(next_deck_button);
      deck_choice_layout.addView(next_deck_button);
    }
        
    Button okay_button = new Button(this);
    okay_button.setText("okay");
    okay_button.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
    okay_button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // recalculate the current deck contents
        Global.current_deck_.clearCards();
        for (int i = 0; i < Global.deck_sub_selections_.size(); ++i) {
          for (int j = 0; j < Global.deck_sub_selections_.get(i).size(); ++j) {
            if (Global.deck_sub_selections_.get(i).get(j)) {
              for (int k = j * 20; (k < (j + 1) * 20) && (k < Global.decks_.get(i).numCards()); ++k) {
                Global.current_deck_.addCardToDeck(Global.decks_.get(i).card(k));
              }
            }
          }
        }
        
        // if after all that there are no cards add the first grade
        if (Global.current_deck_.numCards() == 0) {
          for (int i = 0; i < Global.decks_.get(0).numCards(); ++i) {
            Global.current_deck_.addCardToDeck(Global.decks_.get(0).card(i));
          }
        }
        Intent i = new Intent();
            setResult(RESULT_OK, i);
            finish();
      }
    });
    deck_choice_layout.addView(okay_button);
        
    setButtonState();
  }
  
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    switch (requestCode) {
    case SET_SUB_SELECTION:
      setButtonState();
      break;
    }
  }
  
  public void setButtonState() {
    for (int i = 0; i < Global.decks_.size(); ++i) {
      boolean something_true = false;
      boolean something_false = false;
      for (int j = 0; j < Global.deck_sub_selections_.get(i).size(); ++j) {
        if (Global.deck_sub_selections_.get(i).get(j)) {
          something_true = true;
        } else {
          something_false = true;
        }
      }
      if (!something_true) {
        deck_buttons_.elementAt(i).setTextOn(Global.decks_.get(i).name());
        deck_buttons_.elementAt(i).setChecked(false);
      } else {
        if (something_false) {
          deck_buttons_.elementAt(i).setTextOn(Global.decks_.get(i).name() + " (subset)");
        } else {
          deck_buttons_.elementAt(i).setTextOn(Global.decks_.get(i).name());
        }
        deck_buttons_.elementAt(i).setChecked(true);
      }
      deck_buttons_.elementAt(i).invalidate();
    }
  }
  
  Vector<ToggleButton> deck_buttons_ = new Vector<ToggleButton>();
}
