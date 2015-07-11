package ParseRunners;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.parse4j.ParseException;

/**
 * Created by stevenramzel on 5/26/15.
 */
public class ParseRunner<T> {

    private final Logger logger;
    private ParseRunnable<T> parseRunnable;

    public ParseRunner(Logger logger, ParseRunnable<T> parseRunnable) {
        this.logger = logger;
        this.parseRunnable = parseRunnable;
    }

    public T run() {
        while (true) {
            try {
                return parseRunnable.run();
            } catch (ParseException e) {
                logger.log(Level.INFO, e);
                new WaitRunner(logger).run();
            }
        }
    }

    public static class WaitRunner implements Runnable {

        public static final int WAIT_TIME_MS = 60000;
        public static final int ONE_SECOND = 1000;
        private Logger logger;

        public WaitRunner(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void run() {
            long waitMs = WAIT_TIME_MS;
            synchronized (this) {
                while (waitMs > ONE_SECOND) {
                    try {
                        logger.info("Waiting " + waitMs / ONE_SECOND + " seconds...");
                        wait(waitMs /= 2L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
