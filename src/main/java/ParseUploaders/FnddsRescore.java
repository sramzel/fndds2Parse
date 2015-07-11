package ParseUploaders;

import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.List;

import ParseRunners.ParseRunnable;
import ParseRunners.ParseRunner;

/**
 * Created by stevenramzel on 5/24/15.
 */
public class FnddsRescore {
    public static void main(String[] args) {
        final Logger logger = Utils.initLogger();
        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");
        final ParseBatch[] batch = new ParseBatch[1];
        batch[0] = new ParseBatch();
        final int[] batchSize = {0};
        final ParseQuery<ParseObject> mainFoodDescQuery = ParseQuery.getQuery("MainFoodDesc");
        final List[] dailyValues = new List[1];
        final int[] foodCount = new int[1];

        new ParseRunner<>(logger, () -> {
            dailyValues[0] = ParseQuery.getQuery("DailyValue").find();
            foodCount[0] = mainFoodDescQuery.count();
            return null;
        }).run();
        for (int foodSkip = 0; foodSkip < foodCount[0]; ) {
            mainFoodDescQuery.skip(foodSkip);
            final List[] foodDescs = new List[1];
            new ParseRunner<>(logger, () -> {
                foodDescs[0] = mainFoodDescQuery.find();
                return null;
            }).run();
            for (int foodId = 0; foodId < foodDescs[0].size(); foodId++) {
                logger.info((foodSkip + foodId) + "/" + foodCount[0]);
                ParseObject foodDesc = (ParseObject) foodDescs[0].get(foodId);
                Double totalScore = 0d;
                for (int dvId = 0; dvId < dailyValues[0].size(); dvId++) {
                    final ParseObject dailyValue = (ParseObject) dailyValues[0].get(dvId);
                    final long code = dailyValue.getLong("nutrientCode");
                    double score = foodDesc.getDouble("score" + code);
                    totalScore += score;
                }

                foodDesc.put("dv", totalScore);
                batch[0].updateObject(foodDesc);
                batchSize[0]++;

                if (batchSize[0] == 50) {
                    new ParseRunner<>(logger, () -> {
                        batch[0].batch();
                        return null;
                    }).run();
                    batch[0] = new ParseBatch();
                    batchSize[0] = 0;
                }
            }
            foodSkip += foodDescs[0].size();
        }
        new ParseRunner<>(logger, () -> {
            batch[0].batch();
            return null;
        }).run();
    }
}
