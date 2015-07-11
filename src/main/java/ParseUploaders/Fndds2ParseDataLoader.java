package ParseUploaders;

import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseException;

import java.io.IOException;

import ParseRunners.ParseRunner;

/**
 * Created by stevenramzel on 4/27/15.
 */
public class Fndds2ParseDataLoader {
    public static final String[] CLASS_NAMES = new String[]{
            "MainFoodDesc",
            "AddFoodDesc",
            "FoodWeights",
            "FoodPortionDesc",
            "SubCodeDesc",
            "FoodCodeSubcodeLink",
            "NutrientValues",
            "NutrientDesc",
            "MoistureFatAdj",
            "DailyValue"
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
                    Utils.uploadClass(logger, className);
                } catch (IOException e) {
                    logger.error(e);
                } catch (ParseException e) {
                    logger.error(e);
                    if (e.getCode() == 155) {
                        new ParseRunner.WaitRunner(logger).run();
                        shouldRetry = true;
                        loadingClassId--;
                    }
                }
                logger.info("Loaded all " + className + " entries");
            }
        }
        logger.info("Finished");
    }
}
