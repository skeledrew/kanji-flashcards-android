package com.daverin.KanjiFlashcards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;

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

  public ArrayList<BitSet> create_deck_subselection_array() {
    ArrayList<BitSet> deck_subselection = new ArrayList<BitSet>();
    for (Deck deck : decks_) {
      BitSet next_selection = new BitSet();
      next_selection.set(
          0, KanjiFlashcards.SubsetsForDeckSize(deck.cards().size()), false);
      deck_subselection.add(next_selection);
    }
    return deck_subselection;
  }

  public static DeckCollection ReadDeckCollectionFromXMLFile(
      XmlResourceParser decks_xml) {
    try {
      int next_tag = decks_xml.next();
      List<String> side_order = new ArrayList<String>();
      List<Deck> decks = new ArrayList<Deck>();
      String style = "";
      String current_deck_name = "unknown";
      List<Card> current_deck_cards = new ArrayList<Card>();
      while (next_tag != XmlResourceParser.END_DOCUMENT) {
        if (next_tag == XmlResourceParser.START_TAG) {
          if (decks_xml.getName().equals("grade")) {
            if (!current_deck_cards.isEmpty()) {
              decks.add(new Deck(current_deck_name, current_deck_cards));
            }
            current_deck_name = decks_xml.getAttributeValue(null, "name");
            current_deck_cards.clear();
          } else if (decks_xml.getName().equals("card")) {
            Map<String, String> allSides = new HashMap<String, String>();
            while ((next_tag = decks_xml.next()) != XmlResourceParser.END_TAG) {
              String sideType = decks_xml.getAttributeValue(null, "type");
              decks_xml.next(); // <side>{text}</side>
              String sideValue = decks_xml.getText();
              allSides.put(sideType, sideValue);
              decks_xml.next(); // </side>
            }
            Card next_card = new Card(allSides);
            current_deck_cards.add(next_card);
          } else if (decks_xml.getName().equals("style")) {
            StringBuilder styleBuilder = new StringBuilder();
            while (decks_xml.next() != XmlResourceParser.END_TAG) {
              styleBuilder.append(decks_xml.getText());
            }
            style = styleBuilder.toString();
          } else if (decks_xml.getName().equals("sideOrder")) {
            while ((next_tag = decks_xml.next()) != XmlResourceParser.END_TAG) {
              decks_xml.next(); // <type>{text}</type>
              side_order.add(decks_xml.getText());
              decks_xml.next(); // </type>
            }
          }
        }
        next_tag = decks_xml.next();
      }
      if (current_deck_cards.size() > 0) {
        decks.add(new Deck(current_deck_name, current_deck_cards));
      }
      return new DeckCollection(side_order, style, decks);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (XmlPullParserException e) {
      throw new RuntimeException(e);
    }
  }
}
