package com.daverin.KanjiFlashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of decks, including information on how to display
 * cards in it.
 *
 * @author Matt DeVore
 */
public class DeckCollection {
  private final List<String> side_order_;
  private final String style_;
  private final List<Deck> decks_;

  public DeckCollection(
      List<String> side_order, String style, List<Deck> decks) {
    side_order_
        = Collections.unmodifiableList(new ArrayList<String>(side_order));
    style_ = style;
    decks_ = Collections.unmodifiableList(new ArrayList<Deck>(decks));
  }

  public List<String> side_order() { return side_order_; }

  /**
   * Returns the CSS style that should be used to show the cards.
   *
   * @return the CSS style to show the cards
   */
  public String style() { return style_; }

  public List<Deck> decks() { return decks_; }
}
