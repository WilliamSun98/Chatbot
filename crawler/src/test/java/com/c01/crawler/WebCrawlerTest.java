package com.c01.crawler;

import com.c01.filebuilder.CrawlerFile;
import com.c01.filebuilder.FileBuilder;
import com.c01.filter.CrawlerFilter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class WebCrawlerTest {

  private FileBuilder fb;
  private CrawlerFilter cf;
  private Set<String> allUrlSet;
  private Set<String> relevantSet;
  private String SEED = "https://www.utsc.utoronto.ca/home/";

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @Before
  public void setUp() {
    fb = Mockito.mock(FileBuilder.class);
    cf = Mockito.mock(CrawlerFilter.class);
    allUrlSet = new HashSet<>();
    relevantSet = new HashSet<>();

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @Test
  public void startCrawlingTest() {
    Crawler crawler = new WebCrawler.Builder(SEED)
        .maxDepth(2)
        .maxThreadNumber(3)
        .maxLinks(10)
        .build();

    Whitebox.setInternalState(crawler, "allUrlSet", allUrlSet);
    crawler.start();
    Assert.assertEquals(10, allUrlSet.size());
  }

  @Test
  public void startRelevantNotBeyondLimitTest() {
    Crawler crawler = new WebCrawler.Builder(SEED)
        .maxDepth(2)
        .maxThreadNumber(3)
        .maxLinks(15)
        .build();

    Whitebox.setInternalState(crawler, "relevantSet", relevantSet);
    crawler.start();
    Assert.assertTrue(relevantSet.size() <= 15);
  }

  @Test
  public void startFilterTest() {
    cf.add_key("@!?#something-you-can't-searched-out?!@#");

    Crawler crawler = new WebCrawler.Builder(SEED)
        .maxDepth(2)
        .maxThreadNumber(3)
        .maxLinks(10)
        .addFilter(cf)
        .build();

    Whitebox.setInternalState(crawler, "allUrlSet", allUrlSet);
    Whitebox.setInternalState(crawler, "relevantSet", relevantSet);
    crawler.start();
    Assert.assertEquals(10, allUrlSet.size());
    Assert.assertEquals(0, relevantSet.size());
  }

  @Test
  public void buildFileTest() {
    Crawler crawler = new WebCrawler.Builder(SEED)
        .maxDepth(2)
        .maxThreadNumber(3)
        .maxLinks(10)
        .fileBuilder(fb)
        .build();

    Mockito.doNothing().when(fb).addDoc(Mockito.any());
    Mockito.doNothing().when(fb)
        .addFile(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    Mockito.doNothing().when(fb).buildFile();

    crawler.start();
    crawler.buildFile();

    Mockito.verify(fb, Mockito.atLeastOnce())
        .addFile(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    Mockito.verify(fb, Mockito.atLeastOnce()).addDoc(Mockito.any());
    Mockito.verify(fb, Mockito.atLeastOnce()).buildFile();


  }

  @Test
  public void getFileTest() {
    CrawlerFile file = Mockito.mock(CrawlerFile.class);
    Crawler crawler = new WebCrawler.Builder(SEED)
        .maxDepth(2)
        .maxThreadNumber(3)
        .maxLinks(10)
        .fileBuilder(fb)
        .build();

    Mockito.when(fb.getFile()).thenReturn(file);

    crawler.start();
    crawler.buildFile();

    Assert.assertEquals(file, crawler.getFile());
  }

  @Test
  public void getFilesTest() {

    CrawlerFile f1 = Mockito.mock(CrawlerFile.class);
    CrawlerFile f2 = Mockito.mock(CrawlerFile.class);

    ArrayList<CrawlerFile> files = new ArrayList<>();
    files.add(f1);
    files.add(f2);

    Crawler crawler = new WebCrawler.Builder(SEED)
        .maxDepth(2)
        .maxThreadNumber(3)
        .maxLinks(10)
        .fileBuilder(fb)
        .build();

    Mockito.when(fb.getFiles()).thenReturn(files);

    crawler.start();
    crawler.buildFile();

    Assert.assertSame(files, fb.getFiles());
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

}