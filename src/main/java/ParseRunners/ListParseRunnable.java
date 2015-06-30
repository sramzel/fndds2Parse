package ParseRunners;

import org.apache.log4j.Logger;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import ParseRunners.FlakyListParseRunnable;
import ParseRunners.FlakyParseRunnable;

/**
 * Created by stevenramzel on 5/26/15.
 */
public class ListParseRunnable {

    private ParseQuery<ParseObject> mQuery;
    private final Logger logger;
    private FlakyListParseRunnable flakyParseRunnable;

    public ListParseRunnable(ParseQuery<ParseObject> query, Logger logger, FlakyListParseRunnable flakyParseRunnable) {
        mQuery = query;
        this.logger = logger;
        this.flakyParseRunnable = flakyParseRunnable;
    }

    public void run() {
        int count = new ParseRunnable<>(logger, new FlakyParseRunnable<Integer>() {
            public Integer run() throws ParseException {
                return mQuery.count();
            }
        }).run();
        for (int skip = 0; skip < count; ) {
            mQuery.skip(skip);
            logger.info((skip / (float) count) * 100f + "%");

            new ParseRunnable<>(logger, new FlakyParseRunnable<Void>() {
                @Override
                public Void run() throws ParseException {
                    flakyParseRunnable.run(mQuery.find());
                    return null;
                }
            }).run();
            skip+=mQuery.getLimit();
        }
    }
}
