package ParseUploaders;

import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ParseRunners.BigListParseRunnable;
import ParseRunners.FlakyParseRunnable;
import ParseRunners.ParseRunnable;

/**
 * Created by stevenramzel on 5/24/15.
 */
public class FnddsCaloriePortions {
    public static void main(String[] args) {
        final Logger logger = Utils.initLogger();
        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

        final HashMap<Long, Double> calMinMap = new HashMap<>();
        final HashMap<Long, Double> calMaxMap = new HashMap<>();
        final ParseQuery<ParseObject> portionQuery = ParseQuery.getQuery("FoodWeights");
        ArrayList<String> keys = new ArrayList<>();
        keys.add("foodCode");
        final ParseQuery<ParseObject> mainFoodDescQuery = ParseQuery.getQuery("MainFoodDesc").addAscendingOrder("foodCode");
        mainFoodDescQuery.selectKeys(keys);
        keys = new ArrayList<>();
        keys.add("nutrientValues");
        final ParseQuery<ParseObject> values = ParseQuery.getQuery("NutrientValues").addAscendingOrder("foodCode").whereEqualTo("nutrientCode", 208L);
        portionQuery.limit(1000);
        mainFoodDescQuery.limit(1000);
        values.limit(1000);

        final int count = new ParseRunnable<>(logger, new FlakyParseRunnable<Integer>() {
            @Override
            public Integer run() throws ParseException {
                return portionQuery.count();
            }
        }).run();
        final int[] n = new int[1];
        new BigListParseRunnable(portionQuery, logger, new FlakyParseRunnable<Void>() {
            @Override
            public Void run() {
                List<ParseObject> nutrients = new ParseRunnable<>(logger, new FlakyParseRunnable<List<ParseObject>>() {
                    @Override
                    public List<ParseObject> run() throws ParseException {
                        return portionQuery.find();
                    }
                }).run();
                for (int i = 0; i < nutrients.size(); i++) {
                    ParseObject nutrient = nutrients.get(i);
                    Long foodCode = nutrient.getLong("foodCode");
                    Double weight = nutrient.getDouble("portionWeight");
                    Double lastMin = calMinMap.get(foodCode);
                    if (weight > 0d) {
                        if (lastMin == null || weight < lastMin)
                            calMinMap.put(foodCode, weight);
                        Double lastMax = calMaxMap.get(foodCode);
                        if (lastMax == null || weight > lastMax)
                            calMaxMap.put(foodCode, weight);
                    }
                    logger.info(++n[0] + "/" + count);
                }
                return null;
            }
        }).run();


        final ParseBatch[] batch = new ParseBatch[1];
        batch[0] = new ParseBatch();

        final int foodCount = new ParseRunnable<>(logger, new FlakyParseRunnable<Integer>() {
            public Integer run() throws ParseException {
                return mainFoodDescQuery.count();
            }
        }).run();
        for (int foodSkip = 0; foodSkip < foodCount; ) {
            mainFoodDescQuery.skip(foodSkip);
            final List<ParseObject> foodDescs = new ParseRunnable<>(logger, new FlakyParseRunnable<List<ParseObject>>() {
                public List<ParseObject> run() throws ParseException {
                    return mainFoodDescQuery.find();
                }
            }).run();
            List<ParseObject> cals = new ParseRunnable<>(logger, new FlakyParseRunnable<List<ParseObject>>() {
                public List<ParseObject> run() throws ParseException {
                    return values.find();
                }
            }).run();
            int batchSize = 0;
            for (int foodId = 0; foodId < foodDescs.size(); foodId++) {
                final ParseObject desc = foodDescs.get(foodId);
                double nutrientValue = (cals.get(foodId)).getDouble("nutrientValue");
                long foodCode = desc.getLong("foodCode");
                desc.put("minCal", (int) (calMinMap.get(foodCode) * nutrientValue) / 100);
                desc.put("maxCal", (int) (calMaxMap.get(foodCode) * nutrientValue) / 100);
                batch[0].updateObject(desc);
                batchSize++;
                logger.info((foodSkip + foodId) + "/" + foodCount);
                if (batchSize == 50) {
                    new ParseRunnable<>(logger, new FlakyParseRunnable<Void>() {
                        public Void run() throws ParseException {
                            batch[0].batch();
                            return null;
                        }
                    }).run();
                    batch[0] = new ParseBatch();
                    batchSize = 0;
                }
            }
            foodSkip += foodDescs.size();
        }
        new ParseRunnable<>(logger, new FlakyParseRunnable<Void>() {
            @Override
            public Void run() throws ParseException {
                batch[0].batch();
                return null;
            }
        }).run();
    }
}
