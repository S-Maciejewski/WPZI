package wpzi;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Indexer
{
    public static void main(String args[]) throws IOException {
        Indexer indexer = new Indexer();
        indexer.indexDocuments();
    }

    private void indexDocuments() throws IOException {
        // REMOVE PREVIOUSLY GENERATED INDEX (DONE)
        try
        {
            FileUtils.deleteDirectory(new File(Constants.index_dir));
        } catch (IOException ignored)
        {
        }

        // LOAD HTML DOCUMENTS (TODO)
        ArrayList<Document> documents = getHTMLDocuments();
//        for(Document doc : documents){
//            System.out.println(doc.getField(Constants.filename) + " in list");
//        }


        // CONSTRUCT INDEX (TODO)
        // - Firstly, create Analyzer object (StandardAnalyzer).
        //   (An Analyzer builds TokenStreams, which analyze text.
        //   It thus represents a policy for extracting index terms from text.)
        // - Then, create IndexWriterConfig object that uses standard analyzer
        // - Construct IndexWriter (you can use FSDirectory.open and Paths.get + Constants.index_dir
        // - Add documents to the index
        // - Commit and close the index.

        // ----------------------------------
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
        IndexWriter indexWriter = new IndexWriter(FSDirectory.open((Paths.get(Constants.index_dir))), indexWriterConfig);
        indexWriter.addDocuments(documents);
        indexWriter.commit();
        indexWriter.close();

        // ----------------------------------

    }


    private ArrayList<Document> getHTMLDocuments()
    {
        // This method is finished. Find getHTMLDocument
        File dir = new File("pages");
        File[] files = dir.listFiles();
        if (files != null)
        {
            ArrayList<Document> htmls = new ArrayList<>(files.length);
            for (int id = 0; id < files.length; id++)
            {
                System.out.println("Loading "+ files[id].getName());
                // TODO finish getHTML document
                htmls.add(getHTMLDocument("pages/" + files[id].getName(), id));
            }
            return htmls;
        }
        return null;
    }

    private Document getHTMLDocument(String path, int id)
    {
        File file = new File(path);
        Document document = new Document();

        /*Expert: directly create a field for a document.
        Most users should use one of the sugar subclasses:

        TextField: Reader or String indexed for full-text search
        StringField: String indexed verbatim as a single token
        IntPoint: int indexed for exact/range queries.
        LongPoint: long indexed for exact/range queries.
        FloatPoint: float indexed for exact/range queries.
        DoublePoint: double indexed for exact/range queries.
        SortedDocValuesField: byte[] indexed column-wise for sorting/faceting
        SortedSetDocValuesField: SortedSet<byte[]> indexed column-wise for sorting/faceting
        NumericDocValuesField: long indexed column-wise for sorting/faceting
        SortedNumericDocValuesField: SortedSet<long> indexed column-wise for sorting/faceting
        StoredField: Stored-only value for retrieving in summary results

        A field is a section of a Document.
        Each field has three parts: name, type and value.
        Values may be text (String, Reader or pre-analyzed TokenStream),
        binary (byte[]), or numeric (a Number). Fields are optionally
        stored in the index, so that they may be returned with hits on the document.
        */

        // STORED + INDEXED = field is searchable and results may be highlighted
        // (e.g., document's title may be presented because original content is stored)

        // STORED but not INDEXED = field is not searchable
        // but some metadata may be derived (e.g., document's id)

        // INDEXED but not STORED = field is searchable but results may not be highlighted

        // For the following, it is suggested to use TextField and StoredField

        // TODO create a field that is stored but not indexed
        // and contains document's id
        // IMPORTANT NOTE: use static fields of Constants class to get
        // the keys of the fields (e.g., Constants.id) !
        // ----------------------------------
        StoredField documentId = new StoredField(Constants.id, id);
        // ----------------------------------

        // TODO create a field that is indexed but not stored
        // and contains document's content
        // for this purpose, extract text from the document
        // using Tika ( use getTextFromHTMLFile() <- this method is finished )
        // ----------------------------------
        TextField documentContent = new TextField(Constants.content, getTextFromHTMLFile(file), Field.Store.NO);
        // ----------------------------------

        // TODO create a field that is stored and indexed
        // and contains file name
        // ----------------------------------
        StoredField documentName = new StoredField(Constants.filename, file.getName());
        // ----------------------------------

        // TODO create an INT field (IntPoint) that is indexed
        // and contains file size (bytes, .length())
        // ----------------------------------
        IntPoint fileSizeInt = new IntPoint(Constants.filesize_int, (int)file.length());
        // ----------------------------------
        // //TODO IntPoint is not stored but we want to a file size
        // ... so add another field (StoredField) that stores file size
        // ----------------------------------
        StoredField fileSize = new StoredField(Constants.filesize, file.length());
        // ----------------------------------

        // TODO add fields to the document object
        // ----------------------------------
        document.add(documentId);
        document.add(documentContent);
        document.add(documentName);
        document.add(fileSizeInt);
        document.add(fileSize);
//        System.out.println("-----Document------\n" + document.getField(Constants.content));
        // ----------------------------------


        return document;

    }

    // (DONE)
    private String getTextFromHTMLFile(File file)
    {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputStream;
        try
        {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }

        ParseContext pContext = new ParseContext();

        //Html parser
        HtmlParser htmlparser = new HtmlParser();
        try
        {
            htmlparser.parse(inputStream, handler, metadata, pContext);
        } catch (IOException | SAXException | TikaException e)
        {
            e.printStackTrace();
        }

        return handler.toString();
    }

}
