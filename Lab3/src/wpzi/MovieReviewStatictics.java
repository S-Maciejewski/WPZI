package wpzi;

import com.sun.deploy.util.ArrayUtil;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class MovieReviewStatictics {
    private static final String DOCUMENTS_PATH = "movies/";
    private int _verbCount = 0;
    private int _nounCount = 0;
    private int _adjectiveCount = 0;
    private int _adverbCount = 0;
    private int _totalTokensCount = 0;

    private PrintStream _statisticsWriter;

    private SentenceModel _sentenceModel;
    private TokenizerModel _tokenizerModel;
    private DictionaryLemmatizer _lemmatizer;
    private PorterStemmer _stemmer;
    private POSModel _posModel;
    private TokenNameFinderModel _peopleModel;
    private TokenNameFinderModel _placesModel;
    private TokenNameFinderModel _organizationsModel;

    public static void main(String[] args) {
        MovieReviewStatictics statictics = new MovieReviewStatictics();
        statictics.run();
    }

    private void run() {
        try {
            initModelsStemmerLemmatizer();

            File dir = new File(DOCUMENTS_PATH);
            File[] reviews = dir.listFiles((d, name) -> name.endsWith(".txt"));

            _statisticsWriter = new PrintStream("statistics.txt", "UTF-8");

            Arrays.sort(reviews, Comparator.comparing(File::getName));
            for (File file : reviews) {
                System.out.println("Movie: " + file.getName().replace(".txt", ""));
                _statisticsWriter.println("Movie: " + file.getName().replace(".txt", ""));

                String text = new String(Files.readAllBytes(file.toPath()));
                processFile(text);

                _statisticsWriter.println();
            }

            overallStatistics();
            _statisticsWriter.close();

        } catch (IOException ex) {
            Logger.getLogger(MovieReviewStatictics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initModelsStemmerLemmatizer() {
        try {
//         TODO: load all OpenNLP models (+Porter stemmer + lemmatizer)
//         from files (use class variables)

            _sentenceModel = new SentenceModel(new File("models/en-sent.bin"));
            _tokenizerModel = new TokenizerModel(new File("models/en-token.bin"));
            _lemmatizer = new DictionaryLemmatizer(new File("models/en-lemmatizer.dict"));
            _stemmer = new PorterStemmer();
            _posModel = new POSModel(new File("models/en-pos-maxent.bin"));
            _peopleModel = new TokenNameFinderModel(new File("models/en-ner-person.bin"));
            _placesModel = new TokenNameFinderModel(new File("models/en-ner-location.bin"));
            _organizationsModel = new TokenNameFinderModel(new File("models/en-ner-organization.bin"));

        } catch (IOException ex) {
            Logger.getLogger(MovieReviewStatictics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processFile(String text) {
        // TODO: process the text to find the following statistics:
        // For each movie derive:

        String[] splitText = text.split(" ");

        //    - number of sentences
        int noSentences = 0;

        //    - number of tokens
        int noTokens = 0;


        //    - number of (unique) stemmed forms
        int noStemmed = 0;


        //    - number of (unique) words from a dictionary (lemmatization)
        int noWords = 0;

        //    -  people
        Span people[] = new Span[]{};
        //    - locations
        Span locations[] = new Span[]{};
        //    - organisations
        Span organisations[] = new Span[]{};

        // TODO + compute the following overall (for all movies) POS tagging statistics:
        //    - percentage number of adverbs (class variable, private int _verbCount = 0)
        //    - percentage number of adjectives (class variable, private int _nounCount = 0)
        //    - percentage number of verbs (class variable, private int _adjectiveCount = 0)
        //    - percentage number of nouns (class variable, private int _adverbCount = 0)
        //    + update _totalTokensCount

        // ------------------------------------------------------------------

        // TODO derive sentences (update noSentences variable)
        SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(_sentenceModel);
        noSentences = sentenceDetectorME.sentDetect(text).length;


        // TODO derive tokens and POS tags from text
        // (update noTokens and _totalTokensCount)
        TokenizerME tokenizerME = new TokenizerME(_tokenizerModel);
        String[] tokens = tokenizerME.tokenize(text);
        noTokens = tokens.length;
        POSTaggerME posTaggerME = new POSTaggerME(_posModel);
        String[] tags = posTaggerME.tag(splitText);
        _totalTokensCount += tags.length;


        // TODO perform stemming (use derived tokens)
        // (update noStemmed)
        Set<String> stemmed = new HashSet<>();
        for (String str : splitText) {
            stemmed.add(_stemmer.stem(str));
        }
        System.out.println("Stemmed without duplicates: " + stemmed.toArray().length);
        noStemmed = stemmed.toArray().length;

        ArrayList<String> tokensReplaced = new ArrayList<>();
        for (String token : tokens)
        {
//         use .toLowerCase().replaceAll("[^a-z0-9]", ""); thereafter, ignore "" tokens
            token = token.toLowerCase().replaceAll("[^a-z0-9]", "");
            if(!token.equals("")) tokensReplaced.add(token);
        }
        tokens = tokensReplaced.toArray(new String[0]);

        // TODO perform lemmatization (use derived tokens)
        // (remove "O" from results - non-dictionary forms, update noWords)
        String[] lemmatized = _lemmatizer.lemmatize(splitText, tags);
        System.out.println("Lemmatized with duplicates: " + lemmatized.length);
        ArrayList<String> uniqueLemmatized = new ArrayList<>(new HashSet<>(Arrays.asList(lemmatized)));
        for(String str : uniqueLemmatized){
            if(str.equals("0")) uniqueLemmatized.remove("0");
        }
        System.out.println("Lemmatized without duplicates and 0s: " + uniqueLemmatized.toArray().length);
        noWords = uniqueLemmatized.toArray().length;

        // TODO derive people, locations, organisations (use tokens),
        // (update people, locations, organisations lists).
        NameFinderME nameFinderME = new NameFinderME(_peopleModel);
        people = nameFinderME.find(splitText);
        nameFinderME = new NameFinderME(_placesModel);
        locations = nameFinderME.find(splitText);
        nameFinderME = new NameFinderME(_organizationsModel);
        organisations = nameFinderME.find(splitText);

        // TODO update overall statistics - use tags and check first letters
        // (see https://www.clips.uantwerpen.be/pages/mbsp-tags; first letter = "V" = verb?)

        for(String tag : tags) {
            if(tag.toLowerCase().startsWith("v") || tag.toLowerCase().startsWith("m")) _verbCount++;
            if(tag.toLowerCase().startsWith("n")) _nounCount++;
            if(tag.toLowerCase().startsWith("j")) _adjectiveCount++;
            if(tag.toLowerCase().startsWith("r")) _adverbCount++;
        }

        // ------------------------------------------------------------------

        saveResults("Sentences", noSentences);
        saveResults("Tokens", noTokens);
        saveResults("Stemmed forms (unique)", noStemmed);
        saveResults("Words from a dictionary (unique)", noWords);

        saveNamedEntities("People", people, tokens);
        saveNamedEntities("Locations", locations, tokens);
        saveNamedEntities("Organizations", organisations, tokens);
    }


    private void saveResults(String feature, int count) {
        String s = feature + ": " + count;
        System.out.println("   " + s);
        _statisticsWriter.println(s);
    }

    private void saveNamedEntities(String entityType, Span spans[], String tokens[]) {
        StringBuilder s = new StringBuilder(entityType + ": ");
        for (int sp = 0; sp < spans.length; sp++) {
            for (int i = spans[sp].getStart(); i < spans[sp].getEnd(); i++) {
                s.append(tokens[i]);
                if (i < spans[sp].getEnd() - 1) s.append(" ");
            }
            if (sp < spans.length - 1) s.append(", ");
        }

        System.out.println("   " + s);
        _statisticsWriter.println(s);
    }

    private void overallStatistics() {
        _statisticsWriter.println("---------OVERALL STATISTICS----------");
        DecimalFormat f = new DecimalFormat("#0.00");

        if (_totalTokensCount == 0) _totalTokensCount = 1;
        String verbs = f.format(((double) _verbCount * 100) / _totalTokensCount);
        String nouns = f.format(((double) _nounCount * 100) / _totalTokensCount);
        String adjectives = f.format(((double) _adjectiveCount * 100) / _totalTokensCount);
        String adverbs = f.format(((double) _adverbCount * 100) / _totalTokensCount);

        _statisticsWriter.println("Verbs: " + verbs + "%");
        _statisticsWriter.println("Nouns: " + nouns + "%");
        _statisticsWriter.println("Adjectives: " + adjectives + "%");
        _statisticsWriter.println("Adverbs: " + adverbs + "%");

        System.out.println("---------OVERALL STATISTICS----------");
        System.out.println("Adverbs: " + adverbs + "%");
        System.out.println("Adjectives: " + adjectives + "%");
        System.out.println("Verbs: " + verbs + "%");
        System.out.println("Nouns: " + nouns + "%");
    }

}
