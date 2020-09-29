package com.c01.filter;

import java.util.ArrayList;

/*
 * Default mock Filter for Filter instance in WebCrawler
 */
public class Default_filter extends CrawlerFilter {

	private static Default_filter dFilter = null;
	
	private Default_filter() {
		super(new ArrayList<String>(), 0, 0);
	}
	
	public static Default_filter get_filter() {
		if (dFilter == null) {
			dFilter = new Default_filter();
		}
		return dFilter;
	}

}
