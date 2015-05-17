import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.parse4j.Parse;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by stevenramzel on 4/27/15.
 */
public class Fndds2ParseDataLoader {

    public static final String APPLICATION_ID = "5V6uI4uhzAzzKc4NMIHfyHVjqi6FFBW6eyi3Yy8o";
    public static final String REST_API_KEY = "djPEF0DiqgFz2JqerokYTjX4DuN5e2knm2Rjo8Vv";
    public static final String PATTERN = "%d{dd-MM-yyyy HH:mm:ss} %C %L %-5p:%m%n";
    public static final String FILE_LOG = "file.log";
    public static final String[] CLASS_NAMES = new String[]{
            "MainFoodDesc",
            "AddFoodDesc"
    };

    enum TypeFactory {
        string {
            @Override
            Object next(Scanner scanner) {
                return scanner.next();
            }
        },
        integer {
            @Override
            Object next(Scanner scanner) {
                return scanner.nextLong();
            }
        },
        fraction {
            @Override
            Object next(Scanner scanner) {
                return scanner.nextFloat();
            }
        },
        date {
            @Override
            Object next(Scanner scanner) {
                return scanner.next();
            }
        };

        abstract Object next(Scanner scanner);
    }

    public static void main(String[] args) {
        Logger logger = initLogger();

        Parse.initialize(APPLICATION_ID, REST_API_KEY);
        logger.info("Parse Initialized");

        for (String className : CLASS_NAMES) {
            try {
                Scanner scanner = getScanner(new FileReader("./assets/" + className + ".txt"), 0L);
                logger.info("File loaded");
                ArrayList<Key> keyList = new ArrayList<>();
                Scanner fieldScanner = new Scanner(scanner.nextLine());
                fieldScanner.useDelimiter("~?\\^~?");
                while (fieldScanner.hasNext()) {
                    String next = fieldScanner.next();
                    String[] split = next.split(":");
                    Key key = new Key(split[0], split[1]);
                    keyList.add(key);
                    logger.info("Added Field " + next);
                }
                int count = ParseQuery.getQuery(className).count();
                for (int i = 0; i < count; i++) scanner.nextLine();
                int n = count;
                ParseBatch batcher = new ParseBatch();
                while (scanner.hasNextLine()) {
                    ParseObject parseObject = new ParseObject(className);
                    for (Key key : keyList) {
                        Object next = key.next(scanner);
                        if (!TypeFactory.date.equals(key.mType)) {
                            parseObject.put(key.mKey, next);
                        }
                    }
                    batcher.createObject(parseObject);
                    scanner.nextLine();
                    if (++n%50 == 0) {
                        logger.info("Added entry " + n);
                        batcher.batch();
                        batcher = new ParseBatch();
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                logger.error(e);
            }
        }
    }

    private static Logger initLogger() {
        Logger logger = Logger.getLogger(Fndds2ParseDataLoader.class.getSimpleName());
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        FileAppender fileAppender = null;
        try {
            PatternLayout layout = new PatternLayout(PATTERN);
            fileAppender = new RollingFileAppender(layout, FILE_LOG);
            logger.addAppender(fileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    public static Scanner getScanner(FileReader sce, Long skipLines) throws IOException {
        Scanner scanner = new Scanner(sce);
        for (long n = 0L; n < skipLines; n++) scanner.nextLine();
        scanner.useDelimiter("~?\\^~?");
        return scanner;
    }

    private static class Key {

        private final TypeFactory mType;
        private final String mKey;

        public Key(String key, String type) {
            mKey = key;
            mType = TypeFactory.valueOf(type);
        }

        Object next(Scanner scanner) {
            return mType.next(scanner);
        }
    }
}
