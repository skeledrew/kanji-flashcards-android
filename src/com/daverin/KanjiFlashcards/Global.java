package com.daverin.KanjiFlashcards;

import java.util.HashMap;
import java.util.Vector;

public class Global {
	public static Vector<Deck> decks_;
	public static Vector<Vector<Boolean>> deck_sub_selections_;
	public static int edit_deck_index_;
	public static Deck current_deck_;
	public static HashMap<String, Card> card_map_;
}
