package com.c01.indexer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.List;

import com.c01.filebuilder.CrawlerFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Indexer {

    private static String INDEX_PATH = "./src/main/resources/index";

    public Indexer() {}

    public String getIndexPath() {
        return INDEX_PATH;
    }

    /**
     * Used to index files from command line
     *
     * @param args docs: the directory where all the files to index are, or a single file
     */
    public static void main(String[] args) {
        // mvn exec:java -Dstart-class="com.c01.indexer.Indexer" -Dexec.args="-docs [folder/file]"
        String usage = "javac [-docs DOCS_PATH] [-update]\n";
        String docsPath = null;

        // get args
        for(int i=0;i<args.length;i++) {
            if ("-docs".equals(args[i])) {
                docsPath = args[i+1];
                i++;
            }
        }

        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        // check if directory exists
        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable," +
                    "please check the path");
            System.exit(1);
        }

        try {
            Indexer indexer = new Indexer();
            // initialise indexer
            IndexWriter writer = indexer.createIndexWriter();
            indexer.indexDocs(writer, docDir);

            writer.close();

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public IndexWriter createIndexWriter() {
        try {
            // initialise indexer
            Directory dir = FSDirectory.open(Paths.get(getIndexPath()));

            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            return new IndexWriter(dir, iwc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurses over files and directories found under the given directory.
     *
     * @param writer writer to the index where the given file/dir info will be stored
     * @param path the file to index, or the directory to recurse into to find files to index
     * @throws IOException if there is a low-level I/O error
     */
    public void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        if (!file.getFileName().toString().startsWith(".")) {
                            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                        }
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /**
     * Indexes a single file using the given writer
     *
     * @param writer writer to the index where the given file info will be stored
     * @param file the file to index
     * @param lastModified date last modified of file
     * @throws IOException if there is a low-level I/O error
     */
    public void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        String fileType = FilenameUtils.getExtension(file.getFileName().toString());

        // make a new, empty document
        Document doc = new Document();
        File pdf = file.toFile();
        InputStream stream = Files.newInputStream(file);

        // file is pdf so need parser
        if (fileType.equalsIgnoreCase("pdf")) {
            // initialise pdf parser
            PDDocument pdfDoc = PDDocument.load(pdf);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");

            doc.add(new TextField("contents", stripper.getText(pdfDoc), Field.Store.NO));

            pdfDoc.close();

        } else {
            // Add the contents of the file to a field named "contents". Specify a Reader, so that the text of
            // the file is tokenized and indexed, but not stored
            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
        }

        int typeLength = fileType.length();
        String fileNameType = file.getFileName().toString();
        String fileName = fileNameType.substring(0, fileNameType.length() - typeLength - 1);

        // Use a field that is indexed (i.e. searchable), but don't tokenize the field into separate words
        doc.add(new StringField("title", fileName, Field.Store.YES));

        // add file type
        doc.add(new StringField("type", fileType, Field.Store.YES));

        // add date added
        doc.add(new LongPoint("added", ZonedDateTime.now().toInstant().toEpochMilli()));

        // Use a LongPoint that is indexed (i.e. efficiently filterable with PointRangeQuery)
        doc.add(new LongPoint("modified", lastModified));

        doc.add(new StringField("url", file.toString(), Field.Store.YES));

        // Existing index (an old copy of this document may have been indexed) so we use updateDocument
        // instead to replace the old one matching the exact path, if present
        writer.updateDocument(new Term("url", file.toString()), doc);
        
    }

    /**
     * Indexes a list of CrawlerFiles, to be used directly after web crawling
     *
     * @param files a list of CrawlerFiles to index
     */
    public void indexFromCrawler(List<CrawlerFile> files) {
        try {
            IndexWriter writer = createIndexWriter();

            for (CrawlerFile file : files) {
                try (InputStream stream = Files.newInputStream(Paths.get(file.getPath()))) {
                    Document doc = new Document();

                    // create all the fields needed to index file, same explanation as method above
                    doc.add(new StringField("title", file.getTitle(), Field.Store.YES));
                    doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
                    doc.add(new StringField("type", "html", Field.Store.YES));
                    doc.add(new LongPoint("added", ZonedDateTime.now().toInstant().toEpochMilli()));
                    doc.add(new LongPoint("modified", file.getDateModified().getTime()));
                    doc.add(new StringField("url", file.getUrl(), Field.Store.YES));

                    writer.updateDocument(new Term("url", file.getUrl()), doc);
                }
            }

            writer.close();

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }


    /**
     * Searches index using user query
     *
     * @param userQ a user query string used to search the index
     * @return a list of Lucene Documents that contain the user query
     */
    public Document[] searchIndex(String userQ) {
        Document[] resultDocs = new Document[0];
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(getIndexPath())));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("title", analyzer);

            String special = "title: \"" + userQ.replaceAll("\\s+","") + "\" OR contents:\"" + userQ
                    + "\" OR type:\"" + userQ.replaceAll("\\s+","") + "\"";

            Query query = parser.parse(special);
            TopDocs results = searcher.search(query, 5);
            ScoreDoc[] hits = results.scoreDocs;
            resultDocs = new Document[hits.length];
            for(int i=0;i<hits.length;++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                resultDocs[i] = d;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return resultDocs;
    }
}
