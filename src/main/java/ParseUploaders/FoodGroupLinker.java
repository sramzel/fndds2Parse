package ParseUploaders;

import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import ParseRunners.BatchParseRunnable;
import ParseRunners.FlakyItemParseRunnable;
import ParseRunners.FlakyListParseRunnable;
import ParseRunners.ListParseRunnable;

/**
 * Created by stevenramzel on 4/27/15.
 */
public class FoodGroupLinker {

    public static void main(String[] args) {
        final Logger logger = Utils.initLogger();

        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

        final HashMap<Long, Long> foodCodeMap = new HashMap<>();
        loadMap(logger, foodCodeMap, "FnddsSRLink", "foodCode", "srFoodCode");
        final HashMap<Long, Long> foodGroupMap = new HashMap<>();
        loadMap(logger, foodGroupMap, "MainFoodDescSR", "srFoodCode", "foodGroupCode");
        final ParseQuery<ParseObject> query = new ParseQuery<>("MainFoodDesc");
        query.limit(1000);
        new ListParseRunnable(query, logger, new FlakyListParseRunnable() {
            @Override
            public void run(List<ParseObject> list) throws ParseException {
                new BatchParseRunnable(logger, new FlakyItemParseRunnable() {
                    @Override
                    public boolean run(ParseObject item) throws ParseException {
                        final Long foodCode = item.getLong("foodCode");
                        final Long value = foodGroupMap.get(foodCodeMap.get(foodCode));
                        final boolean changed = value != null;
                        if (changed) item.put("foodGroupCode", value);
                        return changed;
                    }
                }).run(list);
            }
        }).run();
        logger.info("Finished");
    }

    private static void loadMap(Logger logger, HashMap<Long, Long> map, final String className, String key, String valueKey) {
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
                map.put(parseObject.getLong(key), parseObject.getLong(valueKey));
                scanner.nextLine();
                if (++i % 1000 == 0) logger.info(i);
            }
            logger.info("Loaded all " + className + " entries");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
                    try {return scanner.nextFloat();} catch (InputMismatchException e){
                        e.printStackTrace();
                    }
                    return 0.0f;
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