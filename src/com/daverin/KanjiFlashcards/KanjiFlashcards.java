package com.daverin.KanjiFlashcards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

public class KanjiFlashcards extends Activity {
	
	public static final int REVIEW_MODE_ID = Menu.FIRST;
	public static final int PRACTICE_MODE_ID = Menu.FIRST + 1;
	public static final int QUIZ_MODE_ID = Menu.FIRST + 2;
	public static final int QUIZ_SIZE_MENU = Menu.FIRST + 3;
	public static final int QUIZ_20_ID = Menu.FIRST + 4;
	public static final int QUIZ_50_ID = Menu.FIRST + 5;
	public static final int QUIZ_100_ID = Menu.FIRST + 6;
	public static final int CHOOSE_CARDS_ID = Menu.FIRST + 7;
	
	public static final int CONFIGURE_DECKS = 1;
	public static final int QUIZ_RESULTS = 2;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // This will read from an XML file those cards that are available to the system.
        loadAvailableDecks();
        
        // This will read performance metrics that have been stored for the user as well
        // as the current deck configuration (if saved).
        loadSavedConfiguration();
        
        // The interface is designed specifically for japanese kanji character flash cards.
        setContentView(R.layout.kanji_card);
        
        // This will set the initial card to be shown, when the interface is first created.
        showNextCard();
        
        // The yes and no buttons always have the same simple listeners.
        findViewById(R.id.no_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (quiz_mode_) {
					Global.current_deck_.quizWrong();
				} else {
					Global.current_deck_.practiceWrong();
				}
				showNextCard();
			}
        });
        findViewById(R.id.yes_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (quiz_mode_) {
					Global.current_deck_.quizRight();
				} else {
					Global.current_deck_.practiceRight();
				}
				showNextCard();
			}
        });
    }
    
    @Override
    public void onDestroy() {
    	saveCurrentConfiguration();
    	super.onDestroy();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		if (study_mode_) {
    			int width = ((TableLayout)findViewById(R.id.card_layout)).getWidth();
	    		move_backwards_ = event.getX() < (width / 2.0);
	    		showNextCard();
    		} else if (findViewById(R.id.onyomi).getVisibility() == View.INVISIBLE) {
    			findViewById(R.id.onyomi).setVisibility(View.VISIBLE);
    	   		findViewById(R.id.kunyomi).setVisibility(View.VISIBLE);
    	   		findViewById(R.id.english).setVisibility(View.VISIBLE);
    	    	findViewById(R.id.no_button).setVisibility(View.VISIBLE);
    	    	findViewById(R.id.yes_button).setVisibility(View.VISIBLE);
    		}
    		return true;
    	}
    	return false;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(1, REVIEW_MODE_ID, 0, "Study mode");
        menu.add(2, PRACTICE_MODE_ID, 0, "Practice mode");
        SubMenu quiz_size_menu = menu.addSubMenu(3, QUIZ_SIZE_MENU, 0, "Quiz mode");
        quiz_size_menu.add(4, QUIZ_20_ID, 1, "20 questions");
        quiz_size_menu.add(5, QUIZ_50_ID, 2, "50 questions");
        quiz_size_menu.add(6, QUIZ_100_ID, 3, "100 questions");
        menu.add(7, CHOOSE_CARDS_ID, 0, "Choose cards");
        return result;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(1, !study_mode_);
    	menu.setGroupVisible(2, study_mode_ || quiz_mode_);
    	menu.setGroupVisible(3, !quiz_mode_);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CHOOSE_CARDS_ID:
        	Intent i = new Intent(this, ConfigureDecks.class);
        	startActivityForResult(i, CONFIGURE_DECKS);
            return true;
        case REVIEW_MODE_ID:
        	study_mode_ = true;
        	quiz_mode_ = false;
        	Global.current_deck_.setCurrentReviewIndex(Global.current_deck_.numCards());
			move_backwards_ = false;
			showNextCard();
			return true;
        case PRACTICE_MODE_ID:
        	study_mode_ = false;
        	quiz_mode_ = false;
        	Global.current_deck_.startPracticeMode();
        	showNextCard();
        	return true;
        case QUIZ_20_ID:
        	quiz_mode_ = true;
        	study_mode_ = false;
        	Global.current_deck_.startQuizMode(20);
        	showNextCard();
        	return true;
        case QUIZ_50_ID:
        	quiz_mode_ = true;
        	study_mode_ = false;
        	Global.current_deck_.startQuizMode(50);
        	showNextCard();
        	return true;
        case QUIZ_100_ID:
        	quiz_mode_ = true;
        	study_mode_ = false;
        	Global.current_deck_.startQuizMode(100);
        	showNextCard();
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	if (intent != null) {
    		switch(requestCode) {
    		case CONFIGURE_DECKS:
    			study_mode_ = true;
    			quiz_mode_ = false;
    			Global.current_deck_.setCurrentReviewIndex(Global.current_deck_.numCards());
    			move_backwards_ = false;
    			showNextCard();
    			break;
    		case QUIZ_RESULTS:
    			study_mode_ = true;
    			quiz_mode_ = false;
    			Global.current_deck_.setCurrentReviewIndex(Global.current_deck_.numCards());
    			move_backwards_ = false;
    			showNextCard();
    			break;
    		}
    	}
    }
    
    public void showNextCard() {
    	Card c;
    	if (study_mode_) {
    		if (move_backwards_) {
    			c = Global.current_deck_.getPreviousReviewCard();
    		} else {
    			c = Global.current_deck_.getNextReviewCard();
    		}
    	} else if (quiz_mode_) {
    		if (Global.current_deck_.quizDone()) {
    			Intent i = new Intent(this, QuizResults.class);
    			startActivityForResult(i, QUIZ_RESULTS);
    			return;
    		} else {
    			c = Global.current_deck_.getNextQuizCard();
    		}
    	} else {
    		c = Global.current_deck_.getNextPracticeCard();
    	}
    	((TextView)findViewById(R.id.character)).setText(c.character());
   		findViewById(R.id.onyomi).setVisibility(study_mode_ ? View.VISIBLE : View.INVISIBLE);
   		findViewById(R.id.kunyomi).setVisibility(study_mode_ ? View.VISIBLE : View.INVISIBLE);
   		findViewById(R.id.english).setVisibility(study_mode_ ? View.VISIBLE : View.INVISIBLE);
   		findViewById(R.id.overall_score).setVisibility(study_mode_ ? View.VISIBLE : View.INVISIBLE);
   		findViewById(R.id.kanji_number).setVisibility(study_mode_ ? View.VISIBLE : View.INVISIBLE);
    	findViewById(R.id.no_button).setVisibility(View.INVISIBLE);
    	findViewById(R.id.yes_button).setVisibility(View.INVISIBLE);
    	
		((TextView)findViewById(R.id.onyomi)).setText(c.onyomi().toUpperCase());
		((TextView)findViewById(R.id.kunyomi)).setText(c.kunyomi());
		((TextView)findViewById(R.id.english)).setText(c.english());
		double percent_right = (double)c.total_times_right() / c.total_times_shown() * 100.0;
		((ProgressBar)findViewById(R.id.overall_score)).setProgress((int)(percent_right));
    	((TextView)findViewById(R.id.kanji_number)).setText(
    			Integer.toString(Global.current_deck_.getCurrentReviewIndex() + 1) + " / " +
    			Integer.toString(Global.current_deck_.numCards()));
    }
    
    public void loadAvailableDecks() {
    	// This is a static file distributed with the package.
    	XmlResourceParser lessons = getResources().getXml(R.xml.lessons);
    	try {
	    	int next_tag = lessons.next();
	    	Global.decks_ = new Vector<Deck>();
	    	Global.deck_sub_selections_ = new Vector<Vector<Boolean>>();
	    	Global.card_map_ = new HashMap<String, Card>();
	    	Deck current_deck = new Deck("unknown");
	    	while (next_tag != XmlResourceParser.END_DOCUMENT) {
	    		if (next_tag == XmlResourceParser.START_TAG) {
	    			if (lessons.getName().equals("grade")) {
	    				if (current_deck.numCards() > 0) {
	    					Global.decks_.add(current_deck);
	    					Vector<Boolean> subsets = new Vector<Boolean>();
	    					int subset_counter = 0;
	    					while (subset_counter < current_deck.numCards()) {
	    						subsets.add(true);
	    						subset_counter += 20;
	    					}
	    					Global.deck_sub_selections_.add(subsets);
	    				}
	    				current_deck = new Deck(lessons.getAttributeValue(null, "name"));
	    			} else if (lessons.getName().equals("card")) {
	    				Card next_card = new Card(
	    						lessons.getAttributeValue(null, "character"),
	    						lessons.getAttributeValue(null, "onyomi"),
	    						lessons.getAttributeValue(null, "kunyomi"),
	    						lessons.getAttributeValue(null, "english"));
	    				current_deck.addCardToDeck(next_card);
	    				Global.card_map_.put(next_card.character(), next_card);
	    			}
	    		}
	    		next_tag = lessons.next();
	    	}
	    	if (current_deck.numCards() > 0) {
	    		Global.decks_.add(current_deck);
	    		Vector<Boolean> subsets = new Vector<Boolean>();
				int subset_counter = 0;
				while (subset_counter < current_deck.numCards()) {
					subsets.add(true);
					subset_counter += 20;
				}
				Global.deck_sub_selections_.add(subsets);
	    	}
    	} catch (IOException e) {
    	} catch (XmlPullParserException e) {
    	}
    }
    
    public void loadSavedConfiguration() {
    	try {
    		BufferedReader input_file = new BufferedReader(
    				new InputStreamReader(openFileInput("kanji_config")));
    		String next_line = input_file.readLine();
    		while (next_line != null) {
    			if (next_line.equals("characters")) {
    				while ((next_line != null) && (!next_line.equals("configuration"))) {
    					next_line = input_file.readLine();
    					String[] next_vals = next_line.split(",");
    					if (next_vals.length == 3) {
    						Global.card_map_.get(next_vals[0]).set_total_times_right(Integer.valueOf(next_vals[1]));
    						Global.card_map_.get(next_vals[0]).set_total_times_shown(Integer.valueOf(next_vals[2]));
    					}
    				}
    			}
    			if (next_line.equals("configuration")) {
    				Global.current_deck_ = new Deck("current");
    				while ((next_line != null) && (!next_line.equals("characters"))) {
    					next_line = input_file.readLine();
    					if (next_line != null) {
	    					String[] next_vals = next_line.split(",");
	    					if (next_vals.length > 1) {
	    						for (int i = 0; i < Global.decks_.size(); ++i) {
	    							if (Global.decks_.elementAt(i).name().equals(next_vals[0])) {
	    								int sub_index = 0;
	    								while ((sub_index < Global.deck_sub_selections_.elementAt(i).size()) &&
	    										(sub_index + 1 < next_vals.length)) {
	    									Global.deck_sub_selections_.elementAt(i).setElementAt(
	    											Boolean.valueOf(next_vals[sub_index + 1]), sub_index);
	    									if (Boolean.valueOf(next_vals[sub_index + 1])) {
	    										for (int k = sub_index * 20; (k < (sub_index + 1) * 20) && (k < Global.decks_.elementAt(i).numCards()); ++k) {
	    											Global.current_deck_.addCardToDeck(
	    													Global.decks_.elementAt(i).card(k));
	    										}
	    									}
	    									++sub_index;
	    								}
	    							}
	    						}
	    					}
    					}
    				}
    			}
    			if (next_line != null) {
    				next_line = input_file.readLine();
    			}
    		}
    	} catch (FileNotFoundException e) {
    	} catch (IOException e) {
    	}
    	// at the end of all this, if nothing is selected, select the whole first grade
    	if ((Global.current_deck_ == null) || (Global.current_deck_.numCards() == 0)) {
    		Global.current_deck_ = new Deck("current");
    		for (int i = 0; i < Global.decks_.elementAt(0).numCards(); ++i) {
    			Global.current_deck_.addCardToDeck(Global.decks_.elementAt(0).card(i));
    		}
    		for (int i = 0; i < Global.deck_sub_selections_.elementAt(0).size(); ++i) {
    			Global.deck_sub_selections_.elementAt(0).setElementAt(true, i);
    		}
    		for (int i = 1; i < Global.deck_sub_selections_.size(); ++i) {
        		for (int j = 0; j < Global.deck_sub_selections_.elementAt(i).size(); ++j) {
        			Global.deck_sub_selections_.elementAt(i).setElementAt(false, j);
        		}
        	}
    	}
    	
    	Global.current_deck_.setCurrentReviewIndex(Global.current_deck_.numCards());
    	
    }
    
    public void saveCurrentConfiguration() {
    	try {
        	BufferedWriter output_file = new BufferedWriter(
        			new OutputStreamWriter(openFileOutput("kanji_config", 0)));
        	output_file.write("characters\n");
        	Iterator<String> it = Global.card_map_.keySet().iterator();
        	while (it.hasNext()) {
        		String kanji = it.next();
        		if (Global.card_map_.get(kanji).total_times_shown() > 0) {
        			String next_line = Global.card_map_.get(kanji).character() + "," +
        					Integer.toString(Global.card_map_.get(kanji).total_times_right()) + "," +
        					Integer.toString(Global.card_map_.get(kanji).total_times_shown()) + "\n";
        			output_file.write(next_line);
        		}
        	}
        	output_file.write("configuration\n");
        	for (int i = 0; i < Global.deck_sub_selections_.size(); ++i) {
        		String next_line = Global.decks_.elementAt(i).name();
        		for (int j = 0; j < Global.deck_sub_selections_.elementAt(i).size(); ++j) {
        			next_line += "," + Boolean.toString(Global.deck_sub_selections_.elementAt(i).elementAt(j));
        		}
        		output_file.write(next_line + "\n");
        	}
        	output_file.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
    
    boolean move_backwards_ = false;
    boolean study_mode_ = true;
    boolean quiz_mode_ = false;
}