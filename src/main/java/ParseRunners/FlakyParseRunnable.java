package ParseRunners;

import org.parse4j.ParseException;

/**
 * Created by stevenramzel on 6/29/15.
 */
public interface FlakyParseRunnable<T> {
    T run() throws ParseException;
}
