package ParseRunners;

import org.apache.log4j.Logger;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;
import java.util.List;

/**
 * Created by stevenramzel on 5/26/15.
 */
public class BigListParseRunner {

    private ParseQuery<ParseObject> mQuery;
    private final Logger logger;
    private ParseRunnable<Void> parseRunnable;

    public BigListParseRunner(ParseQuery<ParseObject> query, Logger logger, ParseRunnable<Void> parseRunnable) {
        mQuery = query;
        this.logger = logger;
        this.parseRunnable = parseRunnable;
    }

    public void run() {
        mQuery.addDescendingOrder("createdAt");
        int count = new ParseRunner<>(logger, () -> mQuery.count()).run();
        final int allCount = count;
        for (int skip = 0; skip < count; ) {
            if (skip >= 10000){
                mQuery.skip(10000);
                int oldLimit = mQuery.getLimit();
                mQuery.limit(1);
                new ParseRunner<>(logger, () -> {
                    List<ParseObject> list = mQuery.find();
                    mQuery.whereLessThanOrEqualTo("createdAt", list.get(0).getCreatedAt());
                    return null;
                }).run();
                mQuery.limit(oldLimit);

                count = new ParseRunner<>(logger, () -> mQuery.count()).run();
                skip=0;
            }
            mQuery.skip(skip);
            logger.info((1f - (count - skip) / (float) allCount) * 100f + "%");

            new ParseRunner<>(logger, parseRunnable).run();
            skip+=mQuery.getLimit();
        }
    }
}