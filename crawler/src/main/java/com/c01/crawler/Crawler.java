package com.c01.crawler;

import com.c01.filebuilder.CrawlerFile;
import java.util.List;

public interface Crawler {

  public void start();

  public void buildFile();

  public CrawlerFile getFile();

  public List<CrawlerFile> getFiles();

}
