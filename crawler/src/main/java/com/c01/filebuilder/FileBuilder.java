package com.c01.filebuilder;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.File;
import java.util.Date;
import org.jsoup.nodes.Document;
import java.nio.file.Paths;

public class FileBuilder {

  private ArrayList<CrawlerFile> q;
  private ArrayList<Document> dq;
  private static final String dpath =
      Paths.get(".").toAbsolutePath().normalize().toString() + "/src/main/resources/html";

  public FileBuilder() {
    this.q = new ArrayList<>();
    this.dq = new ArrayList<>();
  }


  public void addDoc(Document doc) {
    dq.add(doc);
  }

  public void addFile(String url, String title, Date lastModifiedTime) {
    CrawlerFile newfile = new CrawlerFile(url, title, lastModifiedTime);
    q.add(newfile);
  }

  public void buildFile() {
    while(!dq.isEmpty()) {
      Document doc = dq.get(0);
      dq.remove(doc);
      String path = createHTMLFile(doc);
      CrawlerFile file = q.get(0);
      q.remove(file);
      if (path != null) {
        file.updatePath(path);
      }
      q.add(file);
    }
  }

  public String createHTMLFile(Document doc) {
    try {
      String name = dpath + "/" +
          System.currentTimeMillis()+".html";
      File newFile = new File(name);
      PrintWriter pw=new PrintWriter(newFile);
      pw.print(doc.toString());
      pw.close();
      return name;
    } catch (Exception e) {
      System.out.println(e);
      return null;
    }
  }

  public CrawlerFile getFile() {
    if (q.isEmpty()) {
      return null;
    } else {
      CrawlerFile lastFile = q.get(0);
      q.remove(lastFile);
      return lastFile;
    }
  }

  public ArrayList<CrawlerFile> getFiles() {
    return q;
  }

  public int getNumberOfFiles() {return q.size();}
}