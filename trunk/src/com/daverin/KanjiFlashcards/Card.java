package com.daverin.KanjiFlashcards;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Card {
	public Card(Map<String, String> sides) {
		sides_ = Collections.unmodifiableMap(
				new HashMap<String, String>(sides));
		total_times_shown_ = 0;
		total_times_right_ = 0;
	}
	
	public Map<String, String> sides() { return sides_; }
	public int total_times_shown() { return total_times_shown_; }
	public void set_total_times_shown(int total_times_shown) { total_times_shown_ = total_times_shown; }
	public int total_times_right() { return total_times_right_; }
	public void set_total_times_right(int total_times_right) { total_times_right_ = total_times_right; }
	public void record_shown(boolean correct) {
		if (correct) { total_times_right_++; }
		total_times_shown_++;
	}

	private Map<String, String> sides_;
	private int total_times_shown_;
	private int total_times_right_;
}
