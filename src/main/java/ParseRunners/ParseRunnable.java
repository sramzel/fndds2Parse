package ParseRunners;

import org.parse4j.ParseException;
import org.parse4j.ParseObject;

/**
 * Created by stevenramzel on 6/29/15.
 */
public interface ParseRunnable<T> {
    T run() throws ParseException;

    interface ListRunnable {
        void run(java.util.List<ParseObject> list) throws ParseException;
    }

    interface ItemRunnable {
        boolean run(ParseObject item) throws ParseException;
    }
}
