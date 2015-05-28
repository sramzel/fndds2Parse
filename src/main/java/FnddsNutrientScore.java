import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by stevenramzel on 5/24/15.
 */
public class FnddsNutrientScore {
    public static void main(String[] args) {
        Logger logger = Utils.initLogger();
        HashMap<Integer, Double> scoreMap = new HashMap<>();
        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

        try {
            List<ParseObject> dailyValues = ParseQuery.getQuery("DailyValue").find();
            for (int n = 0; n < dailyValues.size(); n++) {
                ParseObject dailyValue = dailyValues.get(n);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("NutrientValues");
                query.whereEqualTo("nutrientCode", dailyValue.get("nutrientCode"));

                logger.info("\nScoring " + n * 100 / dailyValues.size() + "%");
                int count = query.count();
                for (int skip = 0; skip < count; skip += 100) {
                    boolean shouldRetry = true;
                    while (shouldRetry) {
                        shouldRetry = false;
                        try {
                            query.skip(skip);
                            List<ParseObject> find = query.find();
                            System.out.print(skip * 100 / count + "%,");
                            for (int i = 0; i < find.size(); i++) {
                                ParseObject parseObject = find.get(i);
                                Integer nutrientCode = (Integer) parseObject.get("nutrientCode");
                                Double score = scoreMap.get(nutrientCode);
                                double nutrientDv = parseObject.getDouble("nutrientValue") / dailyValue.getDouble("nutrientValue");
                                scoreMap.put(nutrientCode, score == null ? nutrientDv : score + nutrientDv);
                            }
                        } catch (ParseException e) {
                            new Utils.WaitRunnable(logger).run();
                            shouldRetry = true;
                        }
                    }
                }
            }

            System.out.println("\nWriting nutrient weights");
            ParseBatch batch;
            Set<Integer> foodCodes = scoreMap.keySet();
            Integer[] nutrientCodeArray = foodCodes.toArray(new Integer[foodCodes.size()]);
            for (int i = 0; i < nutrientCodeArray.length; i++) {
                Integer nutrientCode = nutrientCodeArray[i];
                boolean shouldRetry = true;
                while (shouldRetry) {
                    shouldRetry = false;
                    try {
                        batch = new ParseBatch();
                        int batchSize = 0;
                        System.out.println("\n" + i * 100 / nutrientCodeArray.length + "%,");
                        ParseQuery<ParseObject> portionQuery = ParseQuery.getQuery("DailyValue");
                        portionQuery.whereEqualTo("nutrientCode", nutrientCode);
                        List<ParseObject> find = portionQuery.find();
                        int size = find.size();
                        for (int n = 0; n < size; n++) {
                            ParseObject portion = find.get(n);
                            Double score = scoreMap.get(nutrientCode);
                            portion.put("score", score);
                            batch.updateObject(portion);
                            if (++batchSize == 50) {
                                batch.batch();
                                batch = new ParseBatch();
                                batchSize = 0;
                            }
                        }
                        batch.batch();
                    } catch (ParseException e) {
                        logger.info(e.getMessage());
                        new Utils.WaitRunnable(logger).run();
                        shouldRetry = true;
                        i--;
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
