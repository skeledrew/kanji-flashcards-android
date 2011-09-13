package com.daverin.KanjiFlashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains a list of cards in a particular order and a deck name.
 *
 * @author Joe Daverin
 * @author Matt DeVore
 */
 public class Deck {
   public Deck(String name, List<Card> cards) {
     name_ = name;
     cards_ = Collections.unmodifiableList(new ArrayList<Card>(cards));
   }
  
  public String name() { return name_; }

  public List<Card> cards() { return cards_; }

  private final List<Card> cards_;
  private final String name_;
}
