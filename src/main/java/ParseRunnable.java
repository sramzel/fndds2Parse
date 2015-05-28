import org.apache.log4j.Logger;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.List;

/**
 * Created by stevenramzel on 5/26/15.
 */
class ParseRunnable implements Runnable {

    private final Logger logger;
    private FlakyParseRunnable flakyParseRunnable;

    public ParseRunnable(Logger logger, FlakyParseRunnable flakyParseRunnable) {
        this.logger = logger;
        this.flakyParseRunnable = flakyParseRunnable;
    }

    @Override
    public void run() {
        boolean shouldRetry = true;
        while (shouldRetry) {
            shouldRetry = false;
            try {
                flakyParseRunnable.run();
            } catch (ParseException e) {
                new Utils.WaitRunnable(logger).run();
                shouldRetry = true;
            }
        }
    }

    public interface FlakyParseRunnable {
        void run() throws ParseException;
    }

}
