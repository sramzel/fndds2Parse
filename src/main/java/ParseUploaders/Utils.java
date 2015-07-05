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
    public static final String PATTERN = "%d{dd-MM-yyyy HH:mm:ss} %C %L %-5p:%m%n";
    public static final String FILE_LOG = "file.log";

    static Logger initLogger() {
        Logger logger = Logger.getLogger(Fndds2ParseDataLoader.class.getSimpleName());
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        FileAppender fileAppender;
        try {
            PatternLayout layout = new PatternLayout(PATTERN);
            fileAppender = new RollingFileAppender(layout, FILE_LOG);
            logger.addAppender(fileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
}
