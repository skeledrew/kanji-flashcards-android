package com.daverin.KanjiFlashcards;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
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

  private static final int CARDS_PER_SUBSET = 20;

  public static int SubsetsForDeckSize(int deck_size) {
    return (deck_size + CARDS_PER_SUBSET - 1) / CARDS_PER_SUBSET;
  }

  private Button no_button_, yes_button_;
  private ProgressBar overall_score_;
  private TableLayout card_layout_;
  private TableRow card_tablerow_;
  private TextView kanji_number_;
  private WebView card_webview_;

  private Card current_card_;

  private final View.OnTouchListener screenTouchListener
      = new View.OnTouchListener() {
    public boolean onTouch(View v, MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_UP) {
        if (study_mode_) {
          int width = card_layout_.getWidth();
          move_backwards_ = event.getX() < (width / 2.0);
          showNextCard();
        } else if (no_button_.getVisibility() == View.INVISIBLE) {
          showCard(current_card_.sides(), true);
          no_button_.setVisibility(View.VISIBLE);
          yes_button_.setVisibility(View.VISIBLE);
        }
      }
      return true;
    }
  };

  private void findViews() {
    card_webview_ = (WebView)findViewById(R.id.card_webview);
    yes_button_ = (Button)findViewById(R.id.yes_button);
    no_button_ = (Button)findViewById(R.id.no_button);
    card_layout_ = (TableLayout)findViewById(R.id.card_layout);
    card_tablerow_ = (TableRow)findViewById(R.id.card_tablerow);
    overall_score_ = (ProgressBar)findViewById(R.id.overall_score);
    kanji_number_ = (TextView)findViewById(R.id.kanji_number);
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // This will read from an XML file those cards that are available to the
    // system.
    collection_ = DeckCollection.ReadDeckCollectionFromXMLFile(
        getResources().getXml(R.xml.lessons));

    // This initializes bits for every card set, determining whether it's
    // enabled or disabled in the current configuration.
    deck_sub_selections_ = collection_.create_deck_subselection_array();

    // This will read the current card configuration saved by the user.
    FlashCardDataAdapter flash_card_data = new FlashCardDataAdapter(this);
    flash_card_data.open();
    deck_sub_selections_ =
        flash_card_data.getDeckConfiguration(deck_sub_selections_);
    flash_card_data.close();

    // TODO(jdaverin): verify that the loaded data is valid

    resetCurrentCardsFromSelection();

    setContentView(R.layout.kanji_card);

    findViews();

    card_webview_.setOnTouchListener(screenTouchListener);
    card_tablerow_.setOnTouchListener(screenTouchListener);

    // This will set the initial card to be shown, when the interface is first created.
    startReviewMode();

    // The yes and no buttons always have the same simple listeners.
    no_button_.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if (quiz_mode_) {
          current_deck_.quizWrong();
        } else {
          current_deck_.practiceWrong();
        }
        showNextCard();
      }
    });
    yes_button_.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if (quiz_mode_) {
          current_deck_.quizRight();
        } else {
          current_deck_.practiceRight();
        }
        showNextCard();
      }
    });
  }

  @Override
  public void onPause() {
    // This will save the current card configuration to the data store.
    FlashCardDataAdapter flash_card_data = new FlashCardDataAdapter(this);
    flash_card_data.open();
    flash_card_data.saveDeckConfiguration(deck_sub_selections_);
    flash_card_data.close();
    // saveCurrentConfiguration();
    super.onPause();
  }

  private void showCard(Map<String, String> sides, boolean show_answers) {
    StringBuilder card_html = new StringBuilder();

    card_html.append("<html>")
        .append("<head>");
    if (collection_.style() != null) {
      card_html.append("<style type=\"text/css\">")
          .append(TextUtils.htmlEncode(collection_.style()))
          .append("</style>");
    }
    card_html.append("</head>")
        .append("<body>");
    for (String side : collection_.side_order()) {
      if (sides.containsKey(side)
          && (show_answers || side.equals("character"))) {
        card_html.append("<div class=\"")
            .append(side)
            .append("\">")
            .append(TextUtils.htmlEncode(sides.get(side)))
            .append("</div>");
      }
    }

    card_html.append("</body>");
    card_webview_.loadDataWithBaseURL(null, card_html.toString(), "text/html", HTTP.UTF_8, null);
    card_webview_.setBackgroundColor(0xff000000);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    boolean result = super.onCreateOptionsMenu(menu);
    menu.add(1, REVIEW_MODE_ID, 1, "Study mode");
    menu.add(2, PRACTICE_MODE_ID, 2, "Practice mode");
    SubMenu quiz_size_menu = menu.addSubMenu(3, QUIZ_SIZE_MENU, 3, "Quiz mode");
    quiz_size_menu.add(4, QUIZ_20_ID, 1, "20 questions");
    quiz_size_menu.add(5, QUIZ_50_ID, 2, "50 questions");
    quiz_size_menu.add(6, QUIZ_100_ID, 3, "100 questions");
    menu.add(7, CHOOSE_CARDS_ID, 4, "Choose cards");
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
      ConfigureDecks.Param p = new ConfigureDecks.Param();
      p.selected_subsets_list = new ArrayList<BitSet>(deck_sub_selections_);
      p.deck_names = new ArrayList<String>();
      p.deck_sizes = new ArrayList<Integer>();
      for (Deck deck : collection_.decks()) {
        p.deck_names.add(deck.name());
        p.deck_sizes.add(deck.cards().size());
      }
      p.cards_per_subset = CARDS_PER_SUBSET;
      ConfigureDecks.PARAM.put(i, p);
      startActivityForResult(i, CONFIGURE_DECKS);
      return true;
    case REVIEW_MODE_ID:
      startReviewMode();
      return true;
    case PRACTICE_MODE_ID:
      study_mode_ = false;
      quiz_mode_ = false;
      current_deck_.startPracticeMode();
      showNextCard();
      return true;
    case QUIZ_20_ID:
      quiz_mode_ = true;
      study_mode_ = false;
      current_deck_.startQuizMode(20);
      showNextCard();
      return true;
    case QUIZ_50_ID:
      quiz_mode_ = true;
      study_mode_ = false;
      current_deck_.startQuizMode(50);
      showNextCard();
      return true;
    case QUIZ_100_ID:
      quiz_mode_ = true;
      study_mode_ = false;
      current_deck_.startQuizMode(100);
      showNextCard();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void startReviewMode() {
    study_mode_ = true;
    quiz_mode_ = false;
    current_deck_.setCurrentReviewIndex(current_deck_.cards().size());
    move_backwards_ = false;
    showNextCard();
  }
  
  private void resetCurrentCardsFromSelection() {
    List<Card> study_cards = new ArrayList<Card>();

    // recalculate the current deck contents
    for (int i = 0; i < deck_sub_selections_.size(); ++i) {
      int subset_count
          = SubsetsForDeckSize(collection_.decks().get(i).cards().size());
      for (int j = 0; j < subset_count; ++j) {
        if (deck_sub_selections_.get(i).get(j)) {
          int start_card_index = j * CARDS_PER_SUBSET;
          int end_card_index = Math.min(
              start_card_index + CARDS_PER_SUBSET,
              collection_.decks().get(i).cards().size());
          for (int k = start_card_index; k < end_card_index; ++k) {
            study_cards.add(collection_.decks().get(i).cards().get(k));
          }
        }
      }
    }

    // if after all that there are no cards add the first deck
    if (study_cards.isEmpty()) {
      study_cards.addAll(collection_.decks().get(0).cards());
      for (int i = 0; i < deck_sub_selections_.get(0).size(); ++i) {
        deck_sub_selections_.get(0).set(i, true);
      }
      for (int i = 1; i < deck_sub_selections_.size(); ++i) {
        for (int j = 0; j < deck_sub_selections_.get(i).size(); ++j) {
          deck_sub_selections_.get(i).set(j, false);
        }
      }
    }

    current_deck_ = new StudyDeck(study_cards);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (intent != null) {
      switch(requestCode) {
      case CONFIGURE_DECKS:
        deck_sub_selections_
            = ConfigureDecks.RETURN_SELECTED_SUBSETS_LIST.get(intent);
        resetCurrentCardsFromSelection();
        startReviewMode();
        break;
      }
    }
  }

  public void showNextCard() {
    if (study_mode_) {
      if (move_backwards_) {
        current_card_ = current_deck_.getPreviousReviewCard();
      } else {
        current_card_ = current_deck_.getNextReviewCard();
      }
    } else if (quiz_mode_) {
      if (current_deck_.quizDone()) {
        QuizResults.Param results = new QuizResults.Param();
        results.quiz_percent = (float)current_deck_.quizPercent();
        results.quiz_count_string = current_deck_.quizCountString();
        Intent show_results = new Intent(this, QuizResults.class);
        QuizResults.PARAM.put(show_results, results);
        startActivity(show_results);
        startReviewMode();
        return;
      } else {
        current_card_ = current_deck_.getNextQuizCard();
      }
    } else {
      current_card_ = current_deck_.getNextPracticeCard();
    }
    showCard(current_card_.sides(), study_mode_);
    overall_score_.setVisibility(study_mode_ ? View.VISIBLE : View.INVISIBLE);
    kanji_number_.setVisibility(study_mode_ ? View.VISIBLE : View.INVISIBLE);
    no_button_.setVisibility(View.INVISIBLE);
    yes_button_.setVisibility(View.INVISIBLE);

    overall_score_.setProgress(Math.round(100*(current_deck_.getCurrentReviewIndex() + 1) / current_deck_.cards().size()));
    kanji_number_.setText(
        Integer.toString(current_deck_.getCurrentReviewIndex() + 1) + " / " +
        Integer.toString(current_deck_.cards().size()));
  }

  private DeckCollection collection_;
  private List<BitSet> deck_sub_selections_;
  private StudyDeck current_deck_;
  boolean move_backwards_ = false;
  boolean study_mode_ = true;
  boolean quiz_mode_ = false;
}
