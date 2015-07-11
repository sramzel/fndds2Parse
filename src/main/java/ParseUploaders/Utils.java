package ParseUploaders;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by stevenramzel on 5/24/15.
 */
public class Utils {
    public static final String LOG_PATTERN = "%d{dd-MM-yyyy HH:mm:ss} %C %L %-5p:%m%n";
    public static final String FILE_LOG = "file.log";
    public static final String PATTERN = "~?\\^~?";

    static Logger initLogger() {
        Logger logger = Logger.getLogger(Fndds2ParseDataLoader.class.getSimpleName());
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        FileAppender fileAppender;
        try {
            PatternLayout layout = new PatternLayout(LOG_PATTERN);
            fileAppender = new RollingFileAppender(layout, FILE_LOG);
            logger.addAppender(fileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    static ArrayList<Field> loadClassKeys(Scanner scanner) {
        ArrayList<Field> fieldList = new ArrayList<>();
        Scanner fieldScanner = new Scanner(scanner.nextLine());
        fieldScanner.useDelimiter(PATTERN);
        while (fieldScanner.hasNext()) {
            String next = fieldScanner.next();
            String[] split = next.split(":");
            Field field = new Field(split[0], split[1]);
            fieldList.add(field);
        }
        return fieldList;
    }

    static void loadMap(Logger logger, HashMap<Long, Long> map, final String className, String key, String valueKey) {
        try {
            logger.info("Loading " + className + " class");
            final Scanner scanner = new Scanner(new FileReader("./assets/" + className + ".txt"));
            scanner.useDelimiter("~?\\^~?");
            final ArrayList<Field> fieldList = loadClassKeys(scanner);
            logger.info(className + " file loaded");
            logger.info("Mapping...");
            int i = 0;
            while (scanner.hasNextLine()) {
                ParseObject parseObject = new ParseObject(className);
                for (Field field : fieldList) {
                    Object next = field.next(scanner);
                    if (!Field.Type.date.equals(field.mType)) {
                        parseObject.put(field.mKey, next);
                    }
                }
                final Long keyString = parseObject.getLong(key);
                if (map.get(keyString) == null) {
                    map.put(keyString, parseObject.getLong(valueKey));
                } else {
                    map.put(keyString, 0L);
                }
                scanner.nextLine();
                if (++i % 1000 == 0) logger.info(i);
            }
            logger.info("Loaded all " + className + " entries");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void uploadClass(Logger logger, String className) throws IOException, ParseException {
        logger.info("Loading " + className + " class");
        Scanner scanner = new Scanner(new FileReader("./assets/" + className + ".txt"));
        scanner.useDelimiter("~?\\^~?");
        ArrayList<Field> fieldList1 = new ArrayList<>();
        Scanner fieldScanner = new Scanner(scanner.nextLine());
        fieldScanner.useDelimiter("~?\\^~?");
        while (fieldScanner.hasNext()) {
            String next1 = fieldScanner.next();
            String[] split = next1.split(":");
            Field field1 = new Field(split[0], split[1]);
            fieldList1.add(field1);
        }
        ArrayList<Field> fieldList = fieldList1;
        logger.info(className + " file loaded");

        int count = ParseQuery.getQuery(className).count();
        for (int i = 0; i < count; i++) scanner.nextLine();
        logger.info("Started at line " + count);

        logger.info("Uploading...");
        int count1 = count;
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
            if (++count1 % 50 == 0) {
                batch.batch();
                batch = new ParseBatch();
                logger.info("Added entry " + count1);
            }
        }
        batch.batch();
        logger.info("Added entry " + count1);
    }

    static class Field {

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
}
