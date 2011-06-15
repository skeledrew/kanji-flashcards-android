package com.daverin.KanjiFlashcards;

import java.util.Random;
import java.util.Vector;

public class Deck {
  public Deck(String name) {
    name_ = name;
  }
  
  public String name() { return name_; }
  
  public void addCardToDeck(Card c) {
    cards_.add(c);
  }
  public void clearCards() {
    cards_.clear();
  }
  public int numCards() {
    return cards_.size();
  }
  public Card card(int i) {
    return cards_.elementAt(i);
  }
  
  public void startPracticeMode() {
    practice_cards_.clear();
    for (int i = 0; i < cards_.size(); ++i) {
      practice_cards_.add(cards_.elementAt(i));
    }
    for (int i = 0; i < practice_cards_.size(); ++i) {
      int next_index = rand_gen_.nextInt(practice_cards_.size() - i);
      practice_cards_.insertElementAt(practice_cards_.elementAt(next_index + i), i);
      practice_cards_.removeElementAt(next_index + i + 1);
    }
  }
  
  public Card getNextPracticeCard() {
    return practice_cards_.elementAt(0);
  }
  
  public void practiceRight() {
    practice_cards_.elementAt(0).record_shown(true);
    practice_cards_.add(practice_cards_.elementAt(0));
    practice_cards_.removeElementAt(0);
  }
  
  public void practiceWrong() {
    practice_cards_.elementAt(0).record_shown(false);
    int position = rand_gen_.nextInt(practice_cards_.size() - 1);
    practice_cards_.insertElementAt(practice_cards_.elementAt(0), position + 1);
    practice_cards_.removeElementAt(0);
  }
  
  public void startQuizMode(int quiz_size) {
    quiz_cards_.clear();
    quiz_questions_ = 0;
    quiz_right_ = 0;
    for (int i = 0; i < quiz_size; ++i) {
      int next_index = rand_gen_.nextInt(cards_.size());
      quiz_cards_.add(cards_.elementAt(next_index));
    }
  }
  
  public Card getNextQuizCard() {
    return quiz_cards_.elementAt(0);
  }
  
  public void quizRight() {
    quiz_questions_++;
    quiz_right_++;
    quiz_cards_.removeElementAt(0);
  }
  
  public void quizWrong() {
    quiz_questions_++;
    quiz_cards_.removeElementAt(0);
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
    return cards_.elementAt(current_card_position_);
  }
  
  public Card getPreviousReviewCard() {
    if (cards_.size() == 0) { return null; }
    current_card_position_--;
    if (current_card_position_ < 0) {
      current_card_position_ = cards_.size() - 1;
    }
    return cards_.elementAt(current_card_position_);
  }
  
  int current_card_position_ = 0;
  Vector<Card> cards_ = new Vector<Card>();
  Vector<Card> practice_cards_ = new Vector<Card>();
  Vector<Card> quiz_cards_ = new Vector<Card>();
  int quiz_questions_ = 0;
  int quiz_right_ = 0;
  String name_;
  Random rand_gen_ = new Random();
}
