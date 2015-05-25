import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.IOException;

/**
 * Created by stevenramzel on 5/24/15.
 */
public class Utils {
    static Logger initLogger() {
        Logger logger = Logger.getLogger(Fndds2ParseDataLoader.class.getSimpleName());
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        FileAppender fileAppender;
        try {
            PatternLayout layout = new PatternLayout(Fndds2ParseDataLoader.PATTERN);
            fileAppender = new RollingFileAppender(layout, Fndds2ParseDataLoader.FILE_LOG);
            logger.addAppender(fileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    static class WaitRunnable implements Runnable {

        public static final int WAIT_TIME_MS = 60000;
        public static final int ONE_SECOND = 1000;
        private Logger logger;

        WaitRunnable(Logger logger) {
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
