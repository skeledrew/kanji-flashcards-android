package com.daverin.KanjiFlashcards;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Card {
  public Card(Map<String, String> sides) {
    sides_ = Collections.unmodifiableMap(
        new HashMap<String, String>(sides));
  }

  public Map<String, String> sides() { return sides_; }

  private Map<String, String> sides_;
}
