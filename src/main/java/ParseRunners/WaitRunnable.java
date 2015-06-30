package ParseRunners;

import org.apache.log4j.Logger;

/**
 * Created by stevenramzel on 6/30/15.
 */
public class WaitRunnable implements Runnable {

    public static final int WAIT_TIME_MS = 60000;
    public static final int ONE_SECOND = 1000;
    private Logger logger;

    public WaitRunnable(Logger logger) {
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
