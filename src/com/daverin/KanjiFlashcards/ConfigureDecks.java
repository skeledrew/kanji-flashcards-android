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

public class ConfigureDecks extends Activity {
  public static final int SET_SUB_SELECTION = 1;

  public static class Param implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public ArrayList<BitSet> selected_subsets_list;
    public int cards_per_subset;
    public ArrayList<Integer> deck_sizes;
    public ArrayList<String> deck_names;
  }
  public static final Extra<Param> PARAM = Extra.forSerializable();
  
  public static final Extra<ArrayList<BitSet>> RETURN_SELECTED_SUBSETS_LIST
      = Extra.forSerializable();
  
  private static final Extra<Integer> CONTEXT_DECK_INDEX
      = Extra.forSerializable();

  public class DeckClickListener
      implements View.OnClickListener, View.OnLongClickListener {

    public DeckClickListener(int deck_index) {
      deck_index_ = deck_index;
    }
    
    public void onClick(View v) {
      int subset_count
          = (param_.deck_sizes.get(deck_index_) + param_.cards_per_subset - 1)
          / param_.cards_per_subset;
      boolean checked = deck_buttons_.get(deck_index_).isChecked();
      
      selected_subsets_list_.get(deck_index_).set(0, subset_count, checked);
      deck_buttons_.get(deck_index_)
          .setTextOn(param_.deck_names.get(deck_index_));
    }
    
    public boolean onLongClick(View v) {
      Intent select_subdecks
          = new Intent(v.getContext(), DeckSubSelection.class);
      DeckSubSelection.Param select_subdecks_param
          = new DeckSubSelection.Param();
      select_subdecks_param.selected_subsets
          = (BitSet)selected_subsets_list_.get(deck_index_).clone();
      select_subdecks_param.cards_per_subset = param_.cards_per_subset;
      select_subdecks_param.set_size = param_.deck_sizes.get(deck_index_);

      CONTEXT_DECK_INDEX.put(select_subdecks, deck_index_);
      DeckSubSelection.PARAM.put(select_subdecks, select_subdecks_param);
      startActivityForResult(select_subdecks, SET_SUB_SELECTION);
      return true;
    }

    int deck_index_;

  }
  
  private static ArrayList<BitSet> bitSetListDeepCopy(List<BitSet> bitSetList) {
    ArrayList<BitSet> result = new ArrayList<BitSet>(bitSetList.size());

    for (BitSet bitSet : bitSetList) {
      result.add((BitSet)bitSet.clone());
    }
    return result;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
        
    // The interface is designed specifically for japanese kanji character flash cards.
    setContentView(R.layout.configure_decks);
    LinearLayout deck_choice_layout = (LinearLayout)findViewById(R.id.configure_layout);
    
    param_ = PARAM.get(getIntent());
    selected_subsets_list_ = bitSetListDeepCopy(param_.selected_subsets_list);

    deck_buttons_.clear();
    deck_choice_layout.removeAllViews();
    for (int i = 0; i < param_.deck_names.size(); ++i) {
      ToggleButton next_deck_button = new ToggleButton(this);
      next_deck_button.setLayoutParams(
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.FILL_PARENT,
              LinearLayout.LayoutParams.WRAP_CONTENT));
      next_deck_button.setTextOn(param_.deck_names.get(i));
      next_deck_button.setTextOff(param_.deck_names.get(i));
      DeckClickListener click_listener = new DeckClickListener(i);
      next_deck_button.setOnLongClickListener(click_listener);
      next_deck_button.setOnClickListener(click_listener);
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
        Intent i = new Intent();
        i.putExtras(getIntent().getExtras());
        RETURN_SELECTED_SUBSETS_LIST.put(
            i, bitSetListDeepCopy(selected_subsets_list_));
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
      BitSet subset_selection
          = DeckSubSelection.RETURN_SELECTED_SUBSETS.get(intent);
      int deck_index = CONTEXT_DECK_INDEX.get(intent);
      selected_subsets_list_.set(
          deck_index, (BitSet)subset_selection.clone());
      
      setButtonState();
      break;
    }
  }
  
  public void setButtonState() {
    for (int i = 0; i < param_.deck_names.size(); ++i) {
      boolean something_true = false;
      boolean something_false = false;
      ToggleButton deck_button = deck_buttons_.get(i);
      String deck_name = param_.deck_names.get(i);
      int deck_size = param_.deck_sizes.get(i);
      
      for (int j = 0; j < deck_size; j += param_.cards_per_subset) {
        if (selected_subsets_list_.get(i).get(j / param_.cards_per_subset)) {
          something_true = true;
        } else {
          something_false = true;
        }
      }
      if (!something_true) {
        deck_button.setTextOn(deck_name);
        deck_button.setChecked(false);
      } else {
        if (something_false) {
          deck_button.setTextOn(deck_name + " (subset)");
        } else {
          deck_button.setTextOn(deck_name);
        }
        deck_button.setChecked(true);
      }
      deck_button.invalidate();
    }
  }
  
  private List<BitSet> selected_subsets_list_;
  private Param param_;
  private List<ToggleButton> deck_buttons_ = new ArrayList<ToggleButton>();
}
