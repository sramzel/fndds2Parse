package ParseUploaders;

import org.apache.log4j.Logger;
import org.parse4j.Parse;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.List;

/**
 * Created by stevenramzel on 5/24/15.
 */
public class FnddsLinker {
    public static void main(String[] args) {
        final Logger logger = Utils.initLogger();
        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");

    }
}
