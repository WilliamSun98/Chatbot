package com.c01.filter;

import java.util.List;
import org.jsoup.nodes.Document;
import java.util.regex.*;

public class CrawlerFilter extends Filter  {
	
	private int filter_size;
	// Regex referenced from "https://www.mkyong.com/regular-expressions/"
	private Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-\\\\+]+(\\\\.[_A-Za-z0-9-]+)*\r\n" + 
			"@[A-Za-z0-9-]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$");
	

	public CrawlerFilter(List<String> filter_keys, int max_key_count, int filter_size) {
		super(filter_keys, max_key_count);
		this.filter_size = filter_size;
	}
	
	/*
	 * Return true if input url is not a email address, and vice versa
	 */
	public boolean check_url(String url) {
		Matcher m = emailPattern.matcher(url);
		return m.matches();
	}
	
	/*
	 * Check if the document is relevant enough according to the keywords.
	 * The relevance results from comparing number of appearance of keywords to the filter_size.
	 */
	public boolean check_relevance(Document doc) {
		int word_count = 0;
		
		String content[] = doc.text().split(" ");
		for (String word : content) {
			if (word_count >= this.filter_size) {
				return true;
			}
			if (this.filt_word(word)) {
				word_count += 1;
			}
		}
		return false;
	}

}
