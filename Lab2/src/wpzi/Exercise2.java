package wpzi;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Exercise2 {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private OptimaizeLangDetector langDetector;

    public static void main(String[] args) {
        Exercise2 exercise = new Exercise2();
        exercise.run();
    }

    private void run() {
        try {
            if (!new File("./outputDocuments").exists()) {
                Files.createDirectory(Paths.get("./outputDocuments"));
            }

            initLangDetector();

            File directory = new File("./documents");
            File[] files = directory.listFiles();
            for (File file : files) {
                processFile(file);
            }
        } catch (IOException | SAXException | TikaException | ParseException e) {
            e.printStackTrace();
        }

    }

    private void initLangDetector() throws IOException {
        // TODO initialize language detector (langDetector)
        this.langDetector = new OptimaizeLangDetector();
        this.langDetector.loadModels();
    }

    private void processFile(File file) throws IOException, SAXException, TikaException, ParseException {
        // TODO: extract content, metadata and language from given file


        InputStream stream = new FileInputStream(file);
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler(Integer.MAX_VALUE);
        parser.parse(stream, handler, metadata);

        String language = langDetector.detect(handler.toString()).getLanguage();

        String creator = metadata.get(TikaCoreProperties.CREATOR);

        String created = metadata.get(TikaCoreProperties.CREATED);
        Date cretionDate = null;
        if (created != null) {
            cretionDate = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss'Z'").parse(created);
        }

        String lastMod = metadata.get(TikaCoreProperties.MODIFIED);
        Date modifiedDate= null;
        if (lastMod != null) {
            modifiedDate = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss'Z'").parse(lastMod);
        }

        Detector mimeDetector = parser.getDetector();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
        metadata.add(Metadata.RESOURCE_NAME_KEY, file.getName());
        MediaType mediaType = mimeDetector.detect(bufferedInputStream, metadata);


//        System.out.println("\nName: " + file.getName());
//        System.out.println("Language: " + language);
//        System.out.println("Creator: " + creator);
//        System.out.println("Creation date: " + cretionDate);
//        System.out.println("Last modification: " + modifiedDate);
//        System.out.println("Mime type: " + mediaType);
//        System.out.println("File content:\n" + bodyContentHandler.toString());

        // call saveResult method to save the data
        saveResult(file.getName(), language, creator, cretionDate, modifiedDate, mediaType.toString(), handler.toString()); //TODO: fill with proper values
    }

    private void saveResult(String fileName, String language, String creatorName, Date creationDate,
                            Date lastModification, String mimeType, String content) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        int index = fileName.lastIndexOf(".");
        String outName = fileName.substring(0, index) + ".txt";
        try {
            PrintWriter printWriter = new PrintWriter("./outputDocuments/" + outName);
            printWriter.write("Name: " + fileName + "\n");
            printWriter.write("Language: " + (language != null ? language : "") + "\n");
            printWriter.write("Creator: " + (creatorName != null ? creatorName : "") + "\n");
            String creationDateStr = creationDate == null ? "" : dateFormat.format(creationDate);
            printWriter.write("Creation date: " + creationDateStr + "\n");
            String lastModificationStr = lastModification == null ? "" : dateFormat.format(lastModification);
            printWriter.write("Last modification: " + lastModificationStr + "\n");
            printWriter.write("MIME type: " + (mimeType != null ? mimeType : "") + "\n");
            printWriter.write("\n");
            printWriter.write(content + "\n");
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
