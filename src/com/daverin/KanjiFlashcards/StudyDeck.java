package com.daverin.KanjiFlashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.util.Log;

/**
 * Represents a deck with studying features. This class stores a deck which is
 * transforming and shuffling as the user studies its cards, and also stores
 * additional data related to the study state.
 *
 * @author Joe Daverin
 * @author Matt DeVore
 */
public class StudyDeck {
  private static final boolean ENABLE_CARD_LOGGING = false;

  public StudyDeck(List<Card> cards) {
    cards_ = Collections.unmodifiableList(new ArrayList<Card>(cards));

    logCards("new StudyDeck", cards_);
  }

  public void startPracticeMode() {
    practice_cards_.clear();
    practice_cards_.addAll(cards_);

    for (int i = 0; i < practice_cards_.size(); ++i) {
      int next_index = rand_gen_.nextInt(practice_cards_.size() - i);
      practice_cards_.add(i, practice_cards_.get(next_index + i));
      practice_cards_.remove(next_index + i + 1);
    }

    logCards("startPracticeMode", practice_cards_);
  }

  public Card getNextPracticeCard() {
    return practice_cards_.get(0);
  }

  public void practiceRight() {
    practice_cards_.add(practice_cards_.get(0));
    practice_cards_.remove(0);

    logCards("practiceRight", practice_cards_);
  }

  public void practiceWrong() {
    int position = rand_gen_.nextInt(practice_cards_.size() - 1);
    practice_cards_.add(position + 1, practice_cards_.get(0));
    practice_cards_.remove(0);

    logCards("practiceWrong", practice_cards_);
  }

  public void startQuizMode(int quiz_size) {
    quiz_cards_.clear();
    quiz_questions_ = 0;
    quiz_right_ = 0;
    for (int i = 0; i < quiz_size; ++i) {
      int next_index = rand_gen_.nextInt(cards_.size());
      quiz_cards_.add(cards_.get(next_index));
    }

    logCards("startQuizMode", quiz_cards_);
  }

  public Card getNextQuizCard() {
    return quiz_cards_.get(0);
  }

  public void quizRight() {
    quiz_questions_++;
    quiz_right_++;
    quiz_cards_.remove(0);

    logCards("quizRight", quiz_cards_);
  }

  public void quizWrong() {
    quiz_questions_++;
    quiz_cards_.remove(0);

    logCards("quizWrong", quiz_cards_);
  }

  public boolean quizDone() {
    return quiz_cards_.size() == 0;
  }

  public double quizPercent() {
    if (quiz_questions_ == 0) {
      return 0.0;
    } else {
      return (double)quiz_right_ / (double)quiz_questions_ * 100.0;
    }
  }

  public String quizCountString() {
    return Integer.toString(quiz_right_) + " / " + Integer.toString(quiz_questions_);
  }

  public int getCurrentReviewIndex() { return current_card_position_; }
  public void setCurrentReviewIndex(int review_index) { current_card_position_ = review_index; }

  public Card getNextReviewCard() {
    if (cards_.size() == 0) { return null; }
    current_card_position_++;
    if (current_card_position_ >= cards_.size()) {
      current_card_position_ = 0;
    }
    return cards_.get(current_card_position_);
  }

  public Card getPreviousReviewCard() {
    if (cards_.size() == 0) { return null; }
    current_card_position_--;
    if (current_card_position_ < 0) {
      current_card_position_ = cards_.size() - 1;
    }
    return cards_.get(current_card_position_);
  }

  public List<Card> cards() { return cards_; }

  private static void logCards(String tag, List<Card> cards) {
    StringBuilder card_list = new StringBuilder();

    boolean enable_card_logging = ENABLE_CARD_LOGGING;
    if (cards.isEmpty() || !enable_card_logging) {
      return;
    }

    // find shortest side
    String shortest_side = null;
    String shortest_side_value = null;

    for (String side : cards.get(0).sides().keySet()) {
      if (shortest_side == null
          || cards.get(0).sides().get(side).length()
              < shortest_side_value.length()) {
        shortest_side = side;
        shortest_side_value = cards.get(0).sides().get(side);
      }
    }

    for (Card card : cards) {
      card_list.append(card.sides().get(shortest_side));
    }

    Log.i("StudyDeck", card_list.toString());
  }

  private final List<Card> cards_;

  private int current_card_position_ = 0;
  private List<Card> practice_cards_ = new ArrayList<Card>();
  private List<Card> quiz_cards_ = new ArrayList<Card>();
  private int quiz_questions_ = 0;
  private int quiz_right_ = 0;
  private Random rand_gen_ = new Random();
}