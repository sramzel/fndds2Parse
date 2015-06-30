package ParseUploaders;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.IOException;

import ParseUploaders.Fndds2ParseDataLoader;

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
}
