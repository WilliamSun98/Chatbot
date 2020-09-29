package com.c01.crawler;

import com.c01.filebuilder.CrawlerFile;
import com.c01.filebuilder.FileBuilder;
import com.c01.filter.CrawlerFilter;
import com.c01.filter.Default_filter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler implements Crawler {

  private int maxDepth;
  private int maxThreads;
  private Integer maxLinks;
  private String seed;
  private CrawlerFilter filter;
  private FileBuilder fb;

  private Set<String> allUrlSet;
  private Set<String> relevantSet;
  private Queue<String> urlQueue;
  private Map<String, Integer> depthLookasideTable;

  private int waitThreadCount;
  private boolean finished;


  private static final Object signal = new Object();

  /**
   * Builder design pattern for WebCrawler.
   */
  public static class Builder {

    private final String seed;

    private int maxDepth = 1;
    private int maxThreads = 10;
    private int maxLinks = Integer.MAX_VALUE;
    private CrawlerFilter filter = Default_filter.get_filter();
    private FileBuilder fb = new FileBuilder();

    public Builder(String seed) {
      this.seed = seed;
    }

    public Builder maxDepth(int depth) {
      this.maxDepth = depth;
      return this;
    }

    public Builder maxThreadNumber(int threadNumber) {
      this.maxThreads = threadNumber;
      return this;
    }

    public Builder maxLinks(int maxLinks) {
      this.maxLinks = maxLinks;
      return this;
    }

    public Builder addFilter(CrawlerFilter filter) {
      this.filter = filter;
      return this;
    }

    public Builder fileBuilder(FileBuilder fb) {
      this.fb = fb;
      return this;
    }

    public WebCrawler build() {
      return new WebCrawler(this);
    }
  }

  private WebCrawler() {
    allUrlSet = new HashSet<>();
    relevantSet = new HashSet<>();
    urlQueue = new LinkedList<>();
    depthLookasideTable = new HashMap<>();

    finished = false;
  }

  private WebCrawler(Builder builder) {
    this();
    this.seed = builder.seed;
    this.maxDepth = builder.maxDepth;
    this.maxThreads = builder.maxThreads;
    this.maxLinks = builder.maxLinks;
    this.filter = builder.filter;
    this.fb = builder.fb;
  }

  /**
   * The function initializes all the threads
   *
   * Threads will wait until the queue has any url to crawl. And, once they
   * finish crawling, then they will wait again.
   */
  private void init() {
    for (int i = 0; i < maxThreads; i++) {
      Thread thread = new Thread(() -> {
        while (!finished) {

          String url = popAUrl();
          if (url == null) {
            synchronized (signal) {
              try {
                waitThreadCount++;
                System.out
                    .println(Thread.currentThread().getName() + ": waiting");
                signal.wait();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          } else {
            crawling(url);
          }
        }
      }, "thread-" + i);

      thread.start();
    }
  }

  /**
   * Starting crawling the url.
   *
   * Get all of its contents, and all sub urls related to it. Then, try to add
   * sub urls into queue for next time crawling.
   *
   * @param url is a input url to crawl
   */
  private void crawling(String url) {
    try {
      Document document = Jsoup.connect(url).get();
      int d = depthLookasideTable.get(url);

      // Check how relevant the document is according to the keywords in filter.
      if (this.filter.check_relevance(document)) {
        // get the last modified date of the url
        URL urlObject = new URL(url);
        URLConnection connection = urlObject.openConnection();
        Date lastModified = new Date(connection.getLastModified());
        // create the relative file with the document, url, title and date
        this.addRelativeFile(document, url, document.title(), lastModified);
        System.out.println(
            Thread.currentThread().getName() + ": crawling " + url
                + ", succeed with depth " + d);
      } else {
        System.out.println(
            Thread.currentThread().getName() + ": crawling " + url
                + ", irrelevant website with depth " + d);
      }

      if (d < maxDepth) {
        Elements elements = document.select("a[href]");
        String subUrl;
        for (Element element : elements) {
          subUrl = element.attr("abs:href");
          if (subUrl != null) {
            synchronized (signal) {
              pushAUrl(subUrl, d + 1);
              if (waitThreadCount > 0) {
                signal.notify();
                waitThreadCount--;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      System.err.println(Thread.currentThread().getName() + ": " + url);
    }
  }

  private synchronized void addRelativeFile(Document document, String url,
      String title, Date date) {
    this.relevantSet.add(url);
    this.fb.addDoc(document);
    this.fb.addFile(url, title, date);
  }

  /**
   * Pop a url from queue.
   *
   * @return a url in queue, null if queue is empty
   */
  private synchronized String popAUrl() {
    if (urlQueue.isEmpty()) {
      return null;
    }
    return urlQueue.poll();
  }

  /**
   * Check if input url is already crawled. If no, then add into set and queue
   * and record its depth.
   *
   * @param url is input url.
   * @param d is the depth level.
   */
  private synchronized void pushAUrl(String url, int d) {
    if (!allUrlSet.contains(url) && allUrlSet.size() < maxLinks) {
      this.allUrlSet.add(url);
      this.urlQueue.add(url);
      this.depthLookasideTable.put(url, d);
    }
  }

  /**
   * Start crawling and check if jobs are all finished.
   *
   * If all jobs are done, then set the flag to true.
   */
  @Override
  public void start() {
    pushAUrl(seed, 1);
    init();

    long start = System.currentTimeMillis();

    while (true) {
      if (isFinished()) {
        long end = System.currentTimeMillis();
        System.out.println("Total Web: " + this.relevantSet.size());
        System.out.println("Time Spent: " + (end - start) / 1000 + " sec");

        finished = true;

        synchronized (signal) {
          signal.notifyAll();
        }

        break;
      }
    }
  }

  /**
   * The function tests if all jobs are done.
   *
   * @return true if there is no job any more, false otherwise
   */
  private boolean isFinished() {
    return urlQueue.isEmpty() && Thread.activeCount() == 1
        || waitThreadCount == maxThreads;
  }

  public void buildFile() {
    this.fb.buildFile();
  }

  public CrawlerFile getFile() {
    return this.fb.getFile();
  }

  @Override
  public List<CrawlerFile> getFiles() {
    return fb.getFiles();
  }

  public static void main(String[] args) {
    List<String> keywords = new ArrayList<>();
    keywords.add("university");
    keywords.add("semester");
    CrawlerFilter filter = new CrawlerFilter(keywords, 5, 1);

    Crawler crawler = new Builder(
        "https://www.google.com/search?source=hp&ei=6tQoXZukH4OgsQXk5LnwBA&q=jobs&oq=jobs&gs_l=psy-ab.3..0i67l10.1606.1938..2224...1.0..0.122.480.4j1......0....1..gws-wiz.....10..35i39j0j0i131.UUdj7eQvgt8&ibp=htl;jobs&sa=X&ved=2ahUKEwj2h5fYhLDjAhXbX80KHcVKCqcQiYsCKAB6BAgDEAM#fpstate=tldetail&htidocid=-FBuEXZbDA7Mm4YrAAAAAA%3D%3D&htivrt=jobs")
        .maxDepth(3)
        .maxThreadNumber(10).maxLinks(150).addFilter(filter).build();
    crawler.start();

//    //this is the command to build those files and create File Objects
//    crawler.buildFile();
//    // this is a small example of information inside the arraylist
//    CrawlerFile file = crawler.getFile();
//    System.out.println(file.getUrl());
//    System.out.println(file.getPath());
//    System.out.println(file.getDateModified());
//    System.out.println(file.getTitle());
//    // second file object test
//    CrawlerFile file1 = crawler.getFile();
//    System.out.println(file1.getUrl());
//    System.out.println(file1.getPath());
//    System.out.println(file1.getDateModified());
//    System.out.println(file1.getTitle());
  }


}
