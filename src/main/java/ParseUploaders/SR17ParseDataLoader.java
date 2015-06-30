package ParseUploaders;

import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import ParseRunners.WaitRunnable;

/**
 * Created by stevenramzel on 4/27/15.
 */
public class SR17ParseDataLoader {

    public static final String[] CLASS_NAMES = new String[]{
            "MainFoodDescSR",
    };

    public static void main(String[] args) {
        Logger logger = Utils.initLogger();

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

        int count = ParseQuery.getQuery(className.substring(0, className.length() - 2)).count();
        for (int i = 0; i < count; i++) scanner.nextLine();
        logger.info("Started at line " + count);

        logger.info("Uploading...");
        uploadClass(logger, className, scanner, fieldList, count);
    }

    private static void uploadClass(Logger logger, String className, Scanner scanner, ArrayList<Field> fieldList, int count) throws ParseException {
        ParseBatch batch = new ParseBatch();
        while (scanner.hasNextLine()) {
            ParseQuery<ParseObject> linkQuery = new ParseQuery<>("");
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

}
