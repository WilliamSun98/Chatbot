package com.c01.crawler;

import com.c01.filebuilder.Job;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class JobCrawlerTest {

  private List<Job> jobs;
  private String KEY = "software engineer";

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @Before
  public void setUp() {
    jobs = new ArrayList<>();

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @Test
  public void startTest() {
    Crawler crawler = new JobCrawler.Builder(KEY)
        .maxLinks(10)
        .build();

    Whitebox.setInternalState(crawler, "jobs", jobs);

    crawler.start();
  }

  @Test
  public void startMaxLimitTest() {
    Crawler crawler = new JobCrawler.Builder(KEY)
        .maxLinks(15)
        .build();

    Whitebox.setInternalState(crawler, "jobs", jobs);

    crawler.start();

    Assert.assertEquals(15, jobs.size());
  }

  @Test
  public void getFileTest() {
    Crawler crawler = new JobCrawler.Builder(KEY)
        .maxLinks(15)
        .build();
    Assert.assertNull(crawler.getFile());
  }

  @Test
  public void getFilesTest() {
    Crawler crawler = new JobCrawler.Builder(KEY)
        .maxLinks(15)
        .build();
    Assert.assertNull(crawler.getFiles());
  }

  @Test
  public void getJobs() {
    Crawler crawler = new JobCrawler.Builder(KEY)
        .maxLinks(15)
        .build();

    crawler.start();

    Assert.assertEquals(15, ((JobCrawler) crawler).getJobs().size());
  }
}