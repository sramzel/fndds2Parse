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

    public static final String PATTERN = "%d{dd-MM-yyyy HH:mm:ss} %C %L %-5p:%m%n";
    public static final String FILE_LOG = "file.log";
    public static final String[] CLASS_NAMES = new String[]{
            "MainFoodDesc",
            "AddFoodDesc"
    };

    public static void main(String[] args) {
        Logger logger = initLogger();

        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

        boolean shouldRetry = true;
        while (shouldRetry) {
            for (int loadingClassId = 0; loadingClassId < CLASS_NAMES.length; loadingClassId++) {
                String className = CLASS_NAMES[loadingClassId];
                shouldRetry = false;
                try {
                    uploadClass(logger, className);
                } catch (IOException e) {
                    logger.error(e);
                } catch (ParseException e) {
                    logger.error(e);
                    if (e.getCode() == 155) {
                        new WaitRunnable(logger).run();
                        shouldRetry = true;
                        loadingClassId--;
                    }
                }
                logger.info("Loaded all " + className + " entries");
            }
        }
        logger.info("Finished");
    }

    private static Logger initLogger() {
        Logger logger = Logger.getLogger(Fndds2ParseDataLoader.class.getSimpleName());
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        FileAppender fileAppender;
        try {
            PatternLayout layout = new PatternLayout(PATTERN);
            fileAppender = new RollingFileAppender(layout, FILE_LOG);
            logger.addAppender(fileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    private static ArrayList<Field> loadClassKeys(Scanner scanner) {
        ArrayList<Field> fieldList = new ArrayList<>();
        Scanner fieldScanner = new Scanner(scanner.nextLine());
        fieldScanner.useDelimiter("~?\\^~?");
        while (fieldScanner.hasNext()) {
            String next = fieldScanner.next();
            String[] split = next.split(":");
            Field field = new Field(split[0], split[1]);
            fieldList.add(field);
        }
        return fieldList;
    }

    private static void uploadClass(Logger logger, String className) throws IOException, ParseException {
        logger.info("Loading " + className + " class");
        Scanner scanner = new Scanner(new FileReader("./assets/" + className + ".txt"));
        scanner.useDelimiter("~?\\^~?");
        ArrayList<Field> fieldList = loadClassKeys(scanner);
        logger.info(className + " file loaded");

        int count = ParseQuery.getQuery(className).count();
        for (int i = 0; i < count; i++) scanner.nextLine();
        logger.info("Started at line " + count);

        logger.info("Uploading...");
        uploadClass(logger, className, scanner, fieldList, count);
    }

    private static void uploadClass(Logger logger, String className, Scanner scanner, ArrayList<Field> fieldList, int count) throws ParseException {
        ParseBatch batch = new ParseBatch();
        while (scanner.hasNextLine()) {
            ParseObject parseObject = new ParseObject(className);
            for (Field field : fieldList) {
                Object next = field.next(scanner);
                if (!Field.Type.date.equals(field.mType)) {
                    parseObject.put(field.mKey, next);
                }
            }
            batch.createObject(parseObject);
            scanner.nextLine();
            if (++count % 50 == 0) {
                batch.batch();
                batch = new ParseBatch();
                logger.info("Added entry " + count);
            }
        }
        batch.batch();
        logger.info("Added entry " + count);
    }

    private static class Field {

        private final Type mType;

        private final String mKey;
        public Field(String key, String type) {
            mKey = key;
            mType = Type.valueOf(type);
        }

        Object next(Scanner scanner) {
            return mType.next(scanner);
        }

        public enum Type {
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
    }

    private static class WaitRunnable implements Runnable {

        public static final int WAIT_TIME_MS = 60000;
        public static final int ONE_SECOND = 1000;
        private Logger logger;

        private WaitRunnable(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void run() {
            long waitMs = WAIT_TIME_MS;
            synchronized (this) {
                while (waitMs > ONE_SECOND) {
                    try {
                        logger.info("Waiting " + waitMs / ONE_SECOND + " seconds...");
                        wait(waitMs /= 2L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
