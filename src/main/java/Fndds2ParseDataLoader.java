import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.parse4j.Parse;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by stevenramzel on 4/27/15.
 */
public class Fndds2ParseDataLoader{

    public static final String CLASS_NAME = Fndds2ParseDataLoader.class.getSimpleName();
    public static final String APPLICATION_ID = "5V6uI4uhzAzzKc4NMIHfyHVjqi6FFBW6eyi3Yy8o";
    public static final String REST_API_KEY = "djPEF0DiqgFz2JqerokYTjX4DuN5e2knm2Rjo8Vv";
    public static final String PATTERN = "%d{dd-MM-yyyy HH:mm:ss} %C %L %-5p:%m%n";

    public static void main(String[] args){
        initLogger();

        Parse.initialize(APPLICATION_ID, REST_API_KEY);
        System.out.println("Parse Initialized");

        try {
            Scanner scanner = getScanner(new FileReader("./assets/mainfooddesc.txt"), 0L);
            System.out.println("File loaded");
            ArrayList<String> hashSet = new ArrayList<>();
            Scanner fieldScanner = new Scanner(scanner.nextLine());
            fieldScanner.useDelimiter("~?\\^~?");
            while (fieldScanner.hasNext()){
                String next = fieldScanner.next();
                hashSet.add(next);
                System.out.println("Added Field" + next);
            }
            int n = 0;
            while (scanner.hasNextLine()){
                ParseObject parseObject = new ParseObject("MainFoodDesc");
                for (String key : hashSet) {
                    if (scanner.hasNext()) {
                        parseObject.put(key, scanner.next());
                    }
                }
                System.out.println("Added entry " + ++n);
                parseObject.save();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static void initLogger() {
        Logger logger = Logger.getLogger(CLASS_NAME);
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        FileAppender fileAppender = null;
        try {
            PatternLayout layout = new PatternLayout(PATTERN);
            fileAppender = new RollingFileAppender(layout,"file.log");
            logger.addAppender(fileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Scanner getScanner(FileReader sce, Long skipLines) throws IOException {
        Scanner scanner = new Scanner(sce);
        for (long n = 0L; n < skipLines; n++) scanner.nextLine();
        scanner.useDelimiter("~?\\^~?");
        return scanner;
    }
}
