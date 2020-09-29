package com.c01.filter;

import java.util.List;

class Filter {
	
	private List<String> filter_keys;
	private int max_key_count;
	
	public Filter(List<String> filter_keys, int count) {
		this.filter_keys = filter_keys;
		this.max_key_count = count;
	}
	
	
	public int add_key(String newkey) {
		if (!this.check_dup(newkey) && this.filter_keys.size() < max_key_count) {
			this.filter_keys.add(newkey);
			return 0;
		}
		return -1;
	}
	
	
	public int remove_key(String targetkey) {
		if (this.filter_keys.remove(targetkey)) {
			return 0;
		}
		return -1;
	}
	
	
	public int set_key(List<String> newkeys) {
		if (newkeys.size() <= max_key_count) {
			this.filter_keys = newkeys;
			return 0;
		}
		this.remove_dup();
		return -1;
	}
	
	/*
	 * Check if the given word contains one of the keyword
	 */
	protected boolean filt_word(String word) {
		for (String key : this.filter_keys) {
			if (word.contains(key)) {
				return true;
			}
		}
		return false;
	}
	
	
	/*
	 * The word to check is not in the list of keywords
	 * Check for loose duplication of the given word and index among the list of keywords.
	 */
	private boolean check_dup(String word) {
		for (String key : this.filter_keys) {
			if (key.contains(word)) {
				return true;
			}
		}
		return false;
	}

	
	/*
	 * The word to check is at index i in the list of keywords
	 * Check for loose duplication of the given word and index among the list of keywords.
	 */
	private boolean check_dup(String word, int currI) {
		int i = 0;
		for (String key : this.filter_keys) {
			if (i != currI && key.contains(word)) {
				return true;
			}
			i ++;
		}
		return false;
	}
	
	/*
	 * Remove the duplicated keyword from the keyword set
	 */
	private void remove_dup() {
		int i = 0;
		for (String key : this.filter_keys) {
			if (this.check_dup(key, i)) {
				this.filter_keys.remove(i);
			}
			else {
				i ++;
			}
		}
	}
	
}
