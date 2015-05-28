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
public class FnddsScore {
    public static void main(String[] args) {
        final Logger logger = Utils.initLogger();
        final HashMap<Integer, Double> scoreMap = new HashMap<>();
        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

        try {
            List<ParseObject> dailyValues = ParseQuery.getQuery("DailyValue").find();
            for (int n = 0; n < dailyValues.size(); n++) {
                ParseObject dailyValue = dailyValues.get(n);
                final ParseQuery<ParseObject> query = ParseQuery.getQuery("NutrientValues");
                query.whereEqualTo("nutrientCode", dailyValue.get("nutrientCode"));
                final double nutrientScore = dailyValue.getDouble("score");
                final double dv = dailyValue.getDouble("nutrientValue");

                logger.info("\nScoring " + n * 100 / dailyValues.size() + "%");
                final int count = query.count();
                for (int skip = 0; skip < count; skip += 100) {
                    final int finalSkip = skip;
                    new ParseRunnable(logger, new ParseRunnable.FlakyParseRunnable() {
                        public void run() throws ParseException {
                            query.skip(finalSkip);
                            List<ParseObject> find = query.find();
                            System.out.print(finalSkip * 100 / count + "%,");
                            for (int i = 0; i < find.size(); i++) {
                                ParseObject parseObject = find.get(i);
                                Integer foodCode = (Integer) parseObject.get("foodCode");
                                Double score = scoreMap.get(foodCode);
                                double nutrientDv = parseObject.getDouble("nutrientValue") / dv / nutrientScore;
                                scoreMap.put(foodCode, score == null ? nutrientDv : score + nutrientDv);
                            }
                        }
                    }).run();
                }
            }

            System.out.println("\nWriting portion scores");
            ParseBatch batch;
            Set<Integer> foodCodes = scoreMap.keySet();
            Integer[] foodCodeArray = foodCodes.toArray(new Integer[foodCodes.size()]);
            for (int i = 0; i < foodCodeArray.length; i++) {
                Integer foodCode = foodCodeArray[i];
                boolean shouldRetry = true;
                while (shouldRetry) {
                    shouldRetry = false;
                    try {
                        batch = new ParseBatch();
                        int batchSize = 0;
                        System.out.println("\n" + i * 100 / foodCodeArray.length + "%,");
                        ParseQuery<ParseObject> portionQuery = ParseQuery.getQuery("FoodWeights");
                        portionQuery.whereEqualTo("foodCode", foodCode);
                        List<ParseObject> find = portionQuery.find();
                        int size = find.size();
                        for (int n = 0; n < size; n++) {
                            ParseObject portion = find.get(n);
                            Object portionWeight = portion.getDouble("portionWeight");
                            Double score = scoreMap.get(foodCode) * (Double) portionWeight;
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
