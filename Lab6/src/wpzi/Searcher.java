package wpzi;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.RangeQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {

    public static void main(String args[]) throws IOException {
        // Load the previously generated index (DONE)
        IndexReader reader = getIndexReader();
        assert reader != null;

        // Construct index searcher (DONE)
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        // Standard analyzer - might be helpful
        Analyzer analyzer = new StandardAnalyzer();

        // TODO your task is to construct several queries and seek for relevant documents

        // TERM QUERY
        // A Query that matches documents containing a term.
        // This may be combined with other terms with a BooleanQuery.
        // TODO seek for documents that contain word mammal
        // as you may notice, this word is not normalized (but is should be normalized
        // in the same way as all documents were normalized when constructing the index.
        // For that reason you can use analyzer object (utf8TOString()!).
        // Then, build a Term object (seek in content - Constants.content) and TermQuery.
        // Lastly, invoke printResultsForQuery.
        String queryMammal = "MaMMal";
        TermQuery tq1;
        {
            // --------------------------------------
            // COMPLETE THE CODE HERE
            System.out.println("1) term query: mammal (CONTENT)");
            String normalized = analyzer.normalize(queryMammal, queryMammal).utf8ToString();
            System.out.println("Normalized query: " + normalized);
            tq1 = new TermQuery(new Term(Constants.content, normalized));
            printResultsForQuery(indexSearcher, tq1);
            // --------------------------------------
        }

        // TODO Repeat the previous step for a word "bird".
        // Compare the results for "mammal" and "bird".
        String queryBird = "bird";
        TermQuery tq2;
        {
            // --------------------------------------
            System.out.println("2) term query bird (CONTENT)");
            String normalized = analyzer.normalize(queryBird, queryBird).utf8ToString();
            System.out.println("Normalized query: " + normalized);
            tq2 = new TermQuery(new Term(Constants.content, normalized));
            printResultsForQuery(indexSearcher, tq2);
            // --------------------------------------
        }

        // TODO now, we seek for documents that contain "mammal" or "bird".
        // Construct two clauses: BooleanClause (use BooleanClause.Occur to set a proper flag).
        // The first concerns tq1 ("mammal") and the second concerns ("bird").
        // To construct BooleanQuery, Use static methods of BooleanQuery.Builder().
        // Additionally, use setMinimumNumberShouldMatch() with a proper parameter
        // to generate "mammal" or "bird" rule.

        // Boolean query
        {
            BooleanClause bc1 = new BooleanClause(tq1, BooleanClause.Occur.SHOULD);
            BooleanClause bc2 = new BooleanClause(tq2, BooleanClause.Occur.SHOULD);
            BooleanQuery.Builder bcb = new BooleanQuery.Builder();
            // --------------------------------------
            bcb.add(bc1);
            bcb.add(bc2);
            bcb.setMinimumNumberShouldMatch(1);
            BooleanQuery bq = bcb.build();
            System.out.println("3) boolean query (CONTENT): mammal or bird");
            printResultsForQuery(indexSearcher, bq);
            // --------------------------------------
        }

        // TODO now, your task is to find all documents which size is smaller than 1000bytes.
        // For this reason, construct Range query.
        // Use IntPoint.newRangeQuery.
        {
            // --------------------------------------
            System.out.println("4) range query: file size in [0b, 1000b]");
            printResultsForQuery(indexSearcher, IntPoint.newRangeQuery(Constants.filesize_int,0,1000));
            // --------------------------------------
        }

        // TODO let's find all documents which name starts with "ant".
        // For this reason, construct PrefixQuery.
        {
            Query pq = new PrefixQuery(new Term(Constants.filename, "ant"));
            // --------------------------------------
            System.out.println("5) Prefix query (FILENAME): ant");
            printResultsForQuery(indexSearcher, pq);

            // --------------------------------------
        }

        // TODO let's build a wildcard query".
        // Construct a WildcardQuery object. Look for documents
        // which contain a term "eat?" "?" stand for any letter (* for a sequence of letters).
        {
            WildcardQuery wq = new WildcardQuery(new Term(Constants.content, "eat?"));
            // --------------------------------------
            System.out.println("6) Wildcard query (CONTENT): eat?");
            printResultsForQuery(indexSearcher, wq);
            // --------------------------------------
        }

        // TODO build a fuzzy query for a word "mamml" (use FuzzyQuerry).
        // Find all documents that contain words which are similar to "mamml".
        // Which documents have been found?
        {
            FuzzyQuery fq = new FuzzyQuery(new Term(Constants.content, "mamml"));
            // --------------------------------------
            System.out.println("7) Fuzzy querry (CONTENT): mamml?");
            printResultsForQuery(indexSearcher, fq);
            // --------------------------------------
        }

        // TODO now, use QueryParser to parse human-entered query strings
        // and generate query object.
        // - use AND, OR , NOT, (, ), + (must), and - (must not) to construct boolean queries
        // - use * and ? to contstruct wildcard queries
        // - use ~ to construct fuzzy (one word, similarity) or proximity (at least two words) queries
        // - use - to construct proximity queries
        // - use \ as an escape character for: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
        // Consider following 5 examples of queries:
        String queryP1 = "MaMMal AND bat";
        String queryP2 = "ante*";
        String queryP3 = "brd~ ";
        String queryP4 = "(\"nocturnal life\"~10) OR bat";
        String queryP5 = "(\"nocturnal life\"~10) OR (\"are nocturnal\"~10)";
        // Select some query:
        String selectedQuery = queryP5;
        // Complete the code here, i.e., build query parser object, parse selected query
        // to query object, and find relevant documents. Analyze the outcomes.
        {
            try {
                Query query = new QueryParser(Constants.content, analyzer).parse(selectedQuery);
                System.out.println("8) query parser = " + selectedQuery);
                printResultsForQuery(indexSearcher, query);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }


        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printResultsForQuery(IndexSearcher indexSearcher, Query q) throws IOException {
        // TODO finish this method
        // - use indexSearcher to search for documents that
        // are relevant according to the query q
        // - Get TopDocs object (number of derived documents = Constant.top_docs)
        // - Iterate over ScoreDocs (in in TopDocs) and print for each document (in separate lines):
        // a) score
        // b) filename
        // c) id
        // d) file size
        // You may use indexSearcher to get a Document object for some docId (ScoreDoc)
        // and use document.get(name of the field) to get the value of id, filename, etc.

        // --------------------------------
        System.out.println("Query string: " + q.toString());
        TopDocs topDocs = indexSearcher.search(q, Constants.top_docs);
        System.out.println("topDocs.totalHints: " + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            System.out.println(scoreDoc.score + ": " + indexSearcher.doc(scoreDoc.doc).getField(Constants.filename) + " (Id=" + scoreDoc.doc + ") (Content=" +
                    indexSearcher.doc(scoreDoc.doc).getField(Constants.content) + ") (Size=" + indexSearcher.doc(scoreDoc.doc).getField(Constants.filesize));
        }
        // --------------------------------
    }

    private static IndexReader getIndexReader() {
        try {
            Directory dir = FSDirectory.open(Paths.get(Constants.index_dir));
            return DirectoryReader.open(dir);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
