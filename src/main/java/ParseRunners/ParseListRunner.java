package ParseRunners;

import org.apache.log4j.Logger;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

/**
 * Created by stevenramzel on 5/26/15.
 */
public class ParseListRunner {

    private ParseQuery<ParseObject> mQuery;
    private final Logger logger;
    private ParseRunnable.ListRunnable parseRunnable;

    public ParseListRunner(ParseQuery<ParseObject> query, Logger logger, ParseRunnable.ListRunnable parseRunnable) {
        mQuery = query;
        this.logger = logger;
        this.parseRunnable = parseRunnable;
    }

    public void run() {
        int count = new ParseRunner<>(logger, () -> mQuery.count()).run();
        for (int skip = 0; skip < count; ) {
            mQuery.skip(skip);
            logger.info((skip / (float) count) * 100f + "%");

            new ParseRunner<>(logger, () -> {
                parseRunnable.run(mQuery.find());
                return null;
            }).run();
            skip+=mQuery.getLimit();
        }
    }
}
