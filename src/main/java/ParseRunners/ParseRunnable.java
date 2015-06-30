package ParseRunners;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.parse4j.ParseException;

import ParseRunners.FlakyParseRunnable;
import ParseRunners.WaitRunnable;

/**
 * Created by stevenramzel on 5/26/15.
 */
public class ParseRunnable<T> {

    private final Logger logger;
    private FlakyParseRunnable<T> flakyParseRunnable;

    public ParseRunnable(Logger logger, FlakyParseRunnable<T> flakyParseRunnable) {
        this.logger = logger;
        this.flakyParseRunnable = flakyParseRunnable;
    }

    public T run() {
        while (true) {
            try {
                return flakyParseRunnable.run();
            } catch (ParseException e) {
                logger.log(Level.INFO, e);
                new WaitRunnable(logger).run();
            }
        }
    }
}
