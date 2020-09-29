package com.c01.crawler;

import com.c01.filebuilder.CrawlerFile;
import com.c01.filebuilder.Job;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JobCrawler implements Crawler {

  private final String BASE_URL = "https://www.linkedin.com/jobs-guest/jobs/api/jobPostings/jobs";
  /**
   * The request can only show 25 links at one time, start could be 0, 25, 50,
   * ... in order to get more job infos
   */
  private final String START_PAGE = "&redirect=false&position=1&pageNum=0&start=%d";
  /**
   * key words are the job title to search
   */
  private final String KEYWORD = "/?keywords=%s";
  /**
   * Types are F, C, P, T, I represents full-time, contract, part-time,
   * temporary, internship respectively connected using "," in url encoding is
   * "%2C"
   */
  private final String JOBTYPE = "&f_JT=%s";

  private final String LOCATION = "&location=%s";

  private Set<String> types;

  private String keyword;
  private int maxLinks;
  private String location;

  private String url;
  public List<Job> jobs;

  private JobCrawler(Builder builder) {
    jobs = new ArrayList<>();
    this.keyword = builder.keyword;
    this.maxLinks = builder.maxLinks;
    this.types = builder.types;
    this.location = builder.location;
    init();
  }

  public static class Builder {

    private final String keyword;

    private int maxLinks = 25;

    /*
     * Jobs types are in a set of its initials
     * Full-time, Contract, Part-time, Temporary, Internship respectively.
     * corresponds to F, C, P, T, I.
     */
    private Set<String> types = new HashSet<>();
    private String location = "Canada";

    public Builder(String keyword) {
      this.keyword = keyword;
    }

    public Builder maxLinks(int maxLinks) {
      this.maxLinks = maxLinks;
      return this;
    }

    public Builder types(Set<String> types) {
      this.types = types;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Crawler build() {
      return new JobCrawler(this);
    }

  }

  private void init() {
    String tmp = next(0);
    if (tmp == null) {
      System.err.println("invalid input");
    } else {
      this.url = tmp;
    }
  }

  private String next(int startNumber) {
    String tmp = null;
    try {
      tmp = BASE_URL + String
          .format(KEYWORD, URLEncoder.encode(keyword, "UTF-8"));
      tmp += String.format(LOCATION, location);
      tmp += String.format(START_PAGE, startNumber);
      if (types != null) {
        String type = String.join(",", types);
        tmp += String.format(JOBTYPE, URLEncoder.encode(type, "UTF-8"));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tmp;
  }

  @Override
  public void start() {
    try {
      Document document = Jsoup.connect(url).get();
      Elements elements = document.select("li");
      // search for the first 25 links
      for (Element element : elements) {
        if (jobs.size() == maxLinks) {
          break;
        }
        Job job = new Job();

        Element div = element.selectFirst("div");
        Element company = div.selectFirst("h4");
        Element detailDiv = div.selectFirst("div");

        job.setJobTitle(div.selectFirst("h3").text());
        job.setJobLink(element.selectFirst("a").attr("href"));

        job.setCompanyName(company.text());
        if (company.selectFirst("a") != null) {
          job.setCompanyLink(company.selectFirst("a").attr("href"));
        }

        job.setLocation(detailDiv.selectFirst("span").text());
        job.setSummery(detailDiv.selectFirst("p").text());

        jobs.add(job);
      }

      // recursively get jobs until hit the maxLinks
      if (jobs.size() < maxLinks) {
        next(jobs.size());
        start();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void buildFile() {
  }

  @Override
  public CrawlerFile getFile() {
    return null;
  }

  @Override
  public List<CrawlerFile> getFiles() {
    return null;
  }

  public List<Job> getJobs() {
    return jobs;
  }

  public static void main(String[] args) {
    Set<String> types = new HashSet<>();
    types.add("I");
    Crawler crawler = new Builder("software engineer")
        .types(types)
        .build();
    crawler.start();
    for (Job job : ((JobCrawler) crawler).getJobs()) {
      System.out.println(job);
    }
  }
}
