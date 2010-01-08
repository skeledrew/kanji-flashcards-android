package com.daverin.KanjiFlashcards;

public class Card {
	public Card(String character, String onyomi, String kunyomi, String english) {
		character_ = character;
		onyomi_ = onyomi;
		kunyomi_ = kunyomi;
		english_ = english;
		total_times_shown_ = 0;
		total_times_right_ = 0;
	}
	
	public String character() { return character_; }
	public String onyomi() { return onyomi_; }
	public String kunyomi() { return kunyomi_; }
	public String english() { return english_; }
	public int total_times_shown() { return total_times_shown_; }
	public void set_total_times_shown(int total_times_shown) { total_times_shown_ = total_times_shown; }
	public int total_times_right() { return total_times_right_; }
	public void set_total_times_right(int total_times_right) { total_times_right_ = total_times_right; }
	public void record_shown(boolean correct) {
		if (correct) { total_times_right_++; }
		total_times_shown_++;
	}
	
	private String character_;
	private String onyomi_;
	private String kunyomi_;
	private String english_;
	private int total_times_shown_;
	private int total_times_right_;
}
