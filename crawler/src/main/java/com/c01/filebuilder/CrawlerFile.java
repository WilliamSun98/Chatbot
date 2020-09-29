package com.c01.filebuilder;


import java.util.Date;

public class CrawlerFile {
  private String url;
  private String path;
  private String title;
  private Date dateModified;

  public CrawlerFile(String url, String title, Date dateModified) {
    this.url = url;
    this.title = title;
    this.dateModified = dateModified;

  }

  public void updatePath(String path) {
    this.path = path;
  }

  public void updateUrl(String url) {
    this.url = url;
  }

  public void updateTitle(String title) {
    this.title = title;
  }

  public void updateDateModified(Date dateModified) {
    this.dateModified = dateModified;
  }

  public String getUrl() {
    return this.url;
  }

  public String getPath(){
    return this.path;
  }

  public Date getDateModified() {
    return this.dateModified;
  }

  public String getTitle() {
    return this.title;
  }
}
