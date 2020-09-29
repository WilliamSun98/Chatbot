package com.c01.urlconnector;

import com.c01.connector.TempBot;
import com.c01.crawler.Crawler;
import com.c01.crawler.JobCrawler;
import com.c01.crawler.WebCrawler.Builder;
import com.c01.filebuilder.Job;
import com.c01.indexer.Indexer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UrlConnector {

  @RequestMapping(value = "/get-answer", method = RequestMethod.POST)
  @CrossOrigin(origins = "http://localhost:4200")
  public ResponseEntity<Object> index(
      @RequestParam Map<String, String> allParams)
      throws IOException {

    Map<String, Object> response = new HashMap<>();

    response.put("watson", null);
    response.put("jobs", null);
    response.put("files", null);

    /*
     * If watson has answer to the question, return watson's answer first.
     */
    String answer = TempBot.getOneResponse(allParams.get("input"));
    if (!answer.equals("I didn't understand can you try again")) {
      response.put("watson", answer);
      return ResponseEntity.ok(response);
    }

    /*
     * If user asks for jobs, then find jobs by crawler in linkedin.
     */
    if ("true".equals(allParams.get("isjob"))) {
      Crawler crawler = new JobCrawler.Builder(allParams.get("input"))
          .maxLinks(5).build();
      crawler.start();

      List<Map<String, String>> jobs = new ArrayList<>();

      for (Job job : ((JobCrawler) crawler).getJobs()) {
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("title", job.getJobTitle());
        jobInfo.put("url", job.getJobLink());
        jobInfo.put("company", job.getCompanyName());
        jobs.add(jobInfo);
      }

      response.put("jobs", jobs);
      return ResponseEntity.ok(response);
    }

    /*
     * if there is an url provided, then start a new thead to crawl the site
     */
    Thread thread = null;
    Indexer indexer = new Indexer();
    if (allParams.containsKey("site") && allParams.get("site").length() > 0) {
      thread = new Thread(() -> {

        Crawler crawler = new Builder(allParams.get("site")).maxDepth(3)
            .maxThreadNumber(10).maxLinks(150).build();
        crawler.start();
        crawler.buildFile();

        indexer.indexFromCrawler(crawler.getFiles());
      });
    }

    Document[] documents = indexer.searchIndex(allParams.get("input"));

    boolean isPDF =
        allParams.containsKey("pdf") && "true".equals(allParams.get("pdf"));

    Date startDate = null;
    Date endDate = null;

    List<Map<String, String>> files = new ArrayList<>();

    /*
     * try to parse all the dates, if failed, then just make them null.
     */
    try {
      startDate =  new SimpleDateFormat(
          "MM/dd/yyyy")
          .parse(allParams.get("startDate"));
      endDate = new SimpleDateFormat("MM/dd/yyyy")
          .parse(allParams.get("endDate"));
    } catch (ParseException e) {
      startDate = null;
      endDate = null;
    }

    /*
     * Get all the files that returned by indexer, and we only return first
     * three files to the frontend.
     */
    int count = 0;
    for (Document document : documents) {
      if (++count > 3) {
        break;
      }
      if (isPDF && !"pdf".equalsIgnoreCase(document.get("type"))) {
        continue;
      }

      Date fileDate = document.get("modified") == null ? null
          : new Date(Long.parseLong(document.get("modified")));

      if (startDate != null && fileDate != null && fileDate.before(startDate)) {
        continue;
      }

      if (endDate != null && fileDate != null && fileDate.after(endDate)) {
        continue;
      }

      Map<String, String> file = new HashMap<>();
      file.put("title", document.get("title"));
      file.put("url", document.get("url"));
      files.add(file);
    }

    /*
     * Start the thread for crawling if site is provided.
     */
    if (thread != null) {
      thread.start();
    }

    response.put("files", files);
    return ResponseEntity.ok(response);

  }

  @RequestMapping(value = "/upload-file", method = RequestMethod.POST)
  @CrossOrigin(origins = "http://localhost:4200")
  public String upload(
      HttpServletRequest request,
      @RequestParam Map<String, MultipartFile> allParams)
      throws IOException {

      File directory = new File("./src/main/resources/html");
      if (! directory.exists()){
          directory.mkdirs();
      }
      MultipartFile mulfile = allParams.get("file");
      String filePath = Paths.get(".").toAbsolutePath().normalize().toString() + "/src/main/resources/html/"
            + mulfile.getOriginalFilename();
      File file = new File(filePath);
      mulfile.transferTo(file);

      Indexer indexer = new Indexer();
      IndexWriter iw = indexer.createIndexWriter();
      indexer.indexDocs(iw, Paths.get(file.getPath()));
      iw.close();

      return "Success";

  }

  @RequestMapping(
      value = "/get-file1",
      method = RequestMethod.GET,
      produces = "application/pdf"
  )
  public ResponseEntity<InputStreamResource> downloadPdf() throws IOException {
    ClassPathResource pdfFile = new ClassPathResource("/Users/apple/Documents/CSCC01/c01summer2019groupproject13/crawler/html/VPMA93.pdf");
    return ResponseEntity
        .ok()
        .contentLength(pdfFile.contentLength())
        .contentType(
            MediaType.parseMediaType("application/octet-stream"))
        .body(new InputStreamResource(pdfFile.getInputStream()));

  }

  @RequestMapping(value="/get-pdf", method=RequestMethod.GET)
  public ResponseEntity<byte[]> getPDF1(@RequestParam String extn, @RequestParam String path) throws IOException {


    HttpHeaders headers = new HttpHeaders();

    if (extn.equals("pdf")) {
      headers.setContentType(MediaType.parseMediaType("application/pdf"));
    } else if (extn.equals("html")) {
      headers.setContentType(MediaType.parseMediaType("text/html"));
    } else if (extn.equals("txt")) {
      headers.setContentType(MediaType.parseMediaType("text/plain"));
    }
    String filename = "file";

    headers.add("content-disposition", "inline;filename=" + filename);

    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
//    String filePath = "/Users/apple/Documents/CSCC01/c01summer2019groupproject13/crawler/html/VPMA93.pdf";
    String filePath = path;
    byte[] pdf1Bytes = Files.readAllBytes(new File(filePath).toPath());
    ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdf1Bytes, headers, HttpStatus.OK);
    return response;

  }
}