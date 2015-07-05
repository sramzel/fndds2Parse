package ParseUploaders;

import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ParseRunners.BatchParseRunnable;
import ParseRunners.BigListParseRunnable;
import ParseRunners.FlakyItemParseRunnable;
import ParseRunners.FlakyListParseRunnable;
import ParseRunners.FlakyParseRunnable;
import ParseRunners.ListParseRunnable;
import ParseRunners.ParseRunnable;

/**
 * Created by stevenramzel on 5/24/15.
 */
public class FnddsScore {

    public static final Long CALORIES_CODE = 208L;

    public static void main(String[] args) {
        final Logger logger = Utils.initLogger();
        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

        final ParseQuery<ParseObject> mainFoodDescQuery = ParseQuery.getQuery("MainFoodDesc");
        {
            ArrayList<String> keys = new ArrayList<>();
            keys.add("foodCode");
            mainFoodDescQuery.selectKeys(keys);
        }
        mainFoodDescQuery.limit(1000);

        final ParseQuery<ParseObject> nutrientValueQuery = new ParseQuery<>("NutrientValues");
        {
            ArrayList<String> keys = new ArrayList<>();
            keys.add("nutrientCode");
            keys.add("foodCode");
            keys.add("nutrientValue");
            nutrientValueQuery.selectKeys(keys);
        }
        nutrientValueQuery.limit(1000);

        final List<ParseObject> dailyValues = new ParseRunnable<>(logger, new FlakyParseRunnable<List<ParseObject>>() {
            public List<ParseObject> run() throws ParseException {
                return ParseQuery.getQuery("DailyValue").find();
            }
        }).run();

        final HashMap<Long, Double> calMap = new HashMap<>();
        final HashMap<Long, HashMap<Long, Double>> scores = new HashMap<>();
        for (int dvId = 0; dvId < dailyValues.size(); dvId++) scores.put(dailyValues.get(dvId).getLong("nutrientCode"), new HashMap<Long, Double>());
        final HashMap<Long, ParseObject> dvMap = new HashMap<>();
        for (ParseObject dv : dailyValues) {
            dvMap.put(dv.getLong("nutrientCode"), dv);
        }
        new BigListParseRunnable(nutrientValueQuery, logger, new FlakyParseRunnable<Void>() {
            public Void run() throws ParseException {
                final List<ParseObject> nutrients = new ParseRunnable<>(logger, new FlakyParseRunnable<List<ParseObject>>() {
                    public List<ParseObject> run() throws ParseException {
                        return nutrientValueQuery.find();
                    }
                }).run();
                for (ParseObject nutrientObject : nutrients) {
                    final Long nutrientCode = nutrientObject.getLong("nutrientCode");
                    final Long foodCode = nutrientObject.getLong("foodCode");
                    final double nutrientValue = nutrientObject.getDouble("nutrientValue");
                    if (CALORIES_CODE.equals(nutrientCode)) {
                        calMap.put(foodCode, nutrientValue / 2000d);
                    } else {
                        ParseObject dvObj = dvMap.get(nutrientCode);
                        if (dvObj != null) {
                            final Double dv = dvObj.getDouble("nutrientValue");
                            scores.get(nutrientCode).put(foodCode, nutrientValue / dv);
                        }
                    }
                }
                return null;
            }
        }).run();

        new ListParseRunnable(mainFoodDescQuery, logger, new FlakyListParseRunnable() {
            @Override
            public void run(List<ParseObject> list) throws ParseException {
                new BatchParseRunnable(logger, new FlakyItemParseRunnable() {
                    @Override
                    public boolean run(ParseObject item) throws ParseException {
                        final Long foodCode = item.getLong("foodCode");
                        Double variance = 0d;
                        Double totalScore = 0d;
                        Double cals = calMap.get(foodCode);
                        for (ParseObject dv : dvMap.values()) {
                            final HashMap<Long, Double> nutrient = scores.get(dv.getLong("nutrientCode"));
                            final double score = cals > 0d ? nutrient.get(foodCode) / cals : 0d;
                            nutrient.put(foodCode, score);
                            totalScore += score;
                            variance += Math.abs(score * score - 1d);
                        }
                        for (ParseObject dv : dvMap.values()) {
                            Long nutrientCode = dv.getLong("nutrientCode");
                            item.put("score" + nutrientCode, scores.get(nutrientCode).get(foodCode));
                        }

                        item.put("dv", totalScore);
                        item.put("score", variance > 0d ? totalScore * totalScore * dailyValues.size()/variance : 0d);
                        return true;
                    }
                }).run(list);
            }
        }).run();
    }
}
