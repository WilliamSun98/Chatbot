package com.c01.indexer;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class IndexerTest {

    private static String TEST_INDEX_PATH = "./src/test/resources/index";
    private static String TEST_FILES_PATH = "./src/test/resources/test-files";

    private Indexer indexer;
    private IndexWriter iw;

    @Before
    public void setUp() {
        File dir = new File(TEST_INDEX_PATH);

        if (! dir.exists()){
            dir.mkdir();
        }

        try {
            FileUtils.cleanDirectory(dir); // clean out directory
            FileUtils.forceDelete(dir); // delete directory
            FileUtils.forceMkdir(dir); // create directory
        } catch (IOException e) {
            e.printStackTrace();
        }

        indexer = Mockito.spy(new Indexer());
        when(indexer.getIndexPath()).thenReturn(TEST_INDEX_PATH);
        iw = indexer.createIndexWriter();
    }

    @Test
    public void indexTxtTest() {
        Path file = Paths.get(TEST_FILES_PATH + "/restapi.txt");

        // try indexing the file
        try {
            BasicFileAttributes basicAttr = Files.readAttributes(file, BasicFileAttributes.class);
            indexer.indexDoc(iw, file, basicAttr.lastModifiedTime().toMillis());
            iw.close();

            // search for the file by file name
            Document[] files = indexer.searchIndex("restapi");

            // check length, title, type
            assertEquals(1, files.length);
            assertEquals("restapi", files[0].get("title"));
            assertEquals("txt", files[0].get("type"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void indexHtmlTest() {
        Path file = Paths.get(TEST_FILES_PATH + "/snowtide.html");

        // try indexing the file
        try {
            BasicFileAttributes basicAttr = Files.readAttributes(file, BasicFileAttributes.class);
            indexer.indexDoc(iw, file, basicAttr.lastModifiedTime().toMillis());
            iw.close();

            // search for the file by file name
            Document[] files = indexer.searchIndex("snowtide");

            // check length, title, type
            assertEquals(1, files.length);
            assertEquals("snowtide", files[0].get("title"));
            assertEquals("html", files[0].get("type"));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void indexPdfTest() {
        Path file = Paths.get(TEST_FILES_PATH + "/rubric.pdf");

        // try indexing the file
        try {
            BasicFileAttributes basicAttr = Files.readAttributes(file, BasicFileAttributes.class);
            indexer.indexDoc(iw, file, basicAttr.lastModifiedTime().toMillis());
            iw.close();

            // search for the file by file name
            Document[] files = indexer.searchIndex("rubric");

            // check length, title, type
            assertEquals(1, files.length);
            assertEquals("rubric", files[0].get("title"));
            assertEquals("pdf", files[0].get("type"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void indexMultipleFilesTest() {
        Path dir = Paths.get(TEST_FILES_PATH);

        // try indexing the files
        try {
            indexer.indexDocs(iw, dir);
            iw.close();

            // search for files
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(TEST_INDEX_PATH)));
            assertEquals(3, reader.numDocs());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryOneFileTest() {
        Path dir = Paths.get(TEST_FILES_PATH);

        // try indexing the files
        try {
            indexer.indexDocs(iw, dir);
            iw.close();

            // search for files by content
            Document[] files = indexer.searchIndex("post get put delete");
            assertEquals(1, files.length);
            assertEquals("restapi", files[0].get("title"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryTwoFilesTest() {
        Path dir = Paths.get(TEST_FILES_PATH);

        // try indexing the files
        try {
            indexer.indexDocs(iw, dir);
            iw.close();

            // search for files by content
            Document[] files = indexer.searchIndex("functionality");
            assertEquals(2, files.length);
            for (Document file : files) {
                String title = file.get("title");
                assertEquals(true, title.equals("rubric") || title.equals("snowtide"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryAllFilesTest() {
        Path dir = Paths.get(TEST_FILES_PATH);

        // try indexing the files
        try {
            indexer.indexDocs(iw, dir);
            iw.close();

            // search for files by content
            Document[] files = indexer.searchIndex("simple");
            assertEquals(3, files.length);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}