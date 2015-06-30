package ParseRunners;

import org.parse4j.ParseException;
import org.parse4j.ParseObject;

import java.util.List;

/**
 * Created by stevenramzel on 6/29/15.
 */
public interface FlakyListParseRunnable {
    void run(List<ParseObject> list) throws ParseException;
}
