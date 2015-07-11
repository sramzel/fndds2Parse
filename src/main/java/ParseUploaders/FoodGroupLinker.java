package ParseUploaders;

import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.HashMap;

import ParseRunners.BatchRunner;
import ParseRunners.ParseListRunner;

/**
 * Created by stevenramzel on 4/27/15.
 */
public class FoodGroupLinker {

    public static void main(String[] args) {
        final Logger logger = Utils.initLogger();

        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

        final HashMap<Long, Long> foodCodeMap = new HashMap<>();
        Utils.loadMap(logger, foodCodeMap, "FnddsSRLink", "foodCode", "srFoodCode");
        final HashMap<Long, Long> foodGroupMap = new HashMap<>();
        Utils.loadMap(logger, foodGroupMap, "MainFoodDescSR", "srFoodCode", "foodGroupCode");
        final ParseQuery<ParseObject> query = new ParseQuery<>("MainFoodDesc");
        query.limit(1000);
        new ParseListRunner(query, logger, list -> new BatchRunner(logger, item -> {
            final Long foodCode = item.getLong("foodCode");
            final Long value = foodGroupMap.get(foodCodeMap.get(foodCode));
            final boolean changed = value != null;
            if (changed) item.put("foodGroupCode", value);
            return changed;
        }).run(list)).run();
        logger.info("Finished");
    }

}