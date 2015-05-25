import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.HashMap;
import java.util.List;

/**
 * Created by stevenramzel on 5/24/15.
 */
public class FnddsScore {
    public static void main(String[] args) {
        Logger logger = Utils.initLogger();
        HashMap<Integer, Integer> scoreMap = new HashMap<>();
        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

        try {
            List<ParseObject> dailyValues = ParseQuery.getQuery("DailyValue").find();
            for (int n = 0; n < dailyValues.size(); n++) {
                ParseObject dailyValue = dailyValues.get(n);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("NutrientValues");
                query.whereEqualTo("nutrientCode", dailyValue.get("nutrientCode"));
                query.addAscendingOrder("nutrientValue");

                System.out.println("\nScoring " + n*100/dailyValues.size() + "%");
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
                                Integer foodCode = (Integer) parseObject.get("foodCode");
                                Integer score = scoreMap.get(foodCode);
                                scoreMap.put(foodCode, score == null ? i : score + i);
                            }
                        } catch (ParseException e) {
                            new Utils.WaitRunnable(logger).run();
                            shouldRetry = true;
                        }
                    }
                }

            }
            ParseQuery<ParseObject> query = ParseQuery.getQuery("MainFoodDesc");
            query.limit(50);
            ParseBatch batch;

            System.out.println("\nWriting scores");
            boolean shouldRetry = true;
            while (shouldRetry) {
                shouldRetry = false;
                try {
                    int count = query.count();
                    for (int i = 0; i < count; i += 50) {
                        shouldRetry = true;
                        while (shouldRetry) {
                            shouldRetry = false;
                            try {
                                batch = new ParseBatch();
                                System.out.print(i * 100 / count + "%,");
                                query.skip(i);
                                for (ParseObject parseObject : query.find()) {
                                    Integer foodCode = (Integer) parseObject.get("foodCode");
                                    Integer value = scoreMap.get(foodCode);
                                    parseObject.put("score", value);
                                    batch.updateObject(parseObject);
                                }
                                batch.batch();
                            } catch (ParseException e) {
                                new Utils.WaitRunnable(logger).run();
                                shouldRetry = true;
                            }
                        }
                    }
                } catch (ParseException e) {
                    logger.info(e.getMessage());
                    new Utils.WaitRunnable(logger).run();
                    shouldRetry = true;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
