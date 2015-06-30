package ParseRunners;

import org.apache.log4j.Logger;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.List;

/**
 * Created by stevenramzel on 5/26/15.
 */
public class BigListParseRunnable {

    private ParseQuery<ParseObject> mQuery;
    private final Logger logger;
    private FlakyParseRunnable<Void> flakyParseRunnable;

    public BigListParseRunnable(ParseQuery<ParseObject> query, Logger logger, FlakyParseRunnable<Void> flakyParseRunnable) {
        mQuery = query;
        this.logger = logger;
        this.flakyParseRunnable = flakyParseRunnable;
    }

    public void run() {
        mQuery.addAscendingOrder("createdAt");
        int count = new ParseRunnable<>(logger, new FlakyParseRunnable<Integer>() {
            public Integer run() throws ParseException {
                return mQuery.count();
            }
        }).run();
        final int allCount = count;
        for (int skip = 0; skip < count; ) {
            if (skip >= 10000){
                mQuery.skip(skip);
                int oldLimit = mQuery.getLimit();
                mQuery.limit(1);
                new ParseRunnable<>(logger, new FlakyParseRunnable<Object>() {
                    public Object run() throws ParseException {
                        List<ParseObject> list = mQuery.find();
                        mQuery.whereGreaterThanOrEqualTo("createdAt", list.get(0).getCreatedAt());
                        return null;
                    }
                }).run();
                mQuery.limit(oldLimit);

                count = new ParseRunnable<>(logger, new FlakyParseRunnable<Integer>() {
                    public Integer run() throws ParseException {return mQuery.count();}}).run();
                skip=0;
            }
            mQuery.skip(skip);
            logger.info((1 - (count - skip) / (float) allCount) * 100f + "%");

            new ParseRunnable<>(logger, flakyParseRunnable).run();
            skip+=mQuery.getLimit();
        }
    }
}
