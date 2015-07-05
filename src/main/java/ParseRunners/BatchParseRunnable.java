package ParseRunners;

import org.apache.log4j.Logger;
import org.parse4j.ParseBatch;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;

import java.util.List;

/**
 * Created by stevenramzel on 6/29/15.
 */
public class BatchParseRunnable implements FlakyListParseRunnable{

    private final Logger logger;
    private final FlakyItemParseRunnable parseRunnable;

    public BatchParseRunnable(Logger logger, FlakyItemParseRunnable parseRunnable) {
        this.logger = logger;
        this.parseRunnable = parseRunnable;
    }

    @Override
    public void run(List<ParseObject> list) {
        ParseBatch batch = new ParseBatch();
        int batchSize = 0;
        for (int i = 0; i < list.size(); i++) {
            final ParseObject item = list.get(i);
            final Boolean changed = new ParseRunnable<>(logger, new FlakyParseRunnable<Boolean>() {
                public Boolean run() throws ParseException {
                    return parseRunnable.run(item);
                }
            }).run();
            if (changed) {
                batch.updateObject(item);
                batchSize++;
                if (batchSize == 50) {
                    final ParseBatch finalBatch = batch;
                    new ParseRunnable<>(logger, new FlakyParseRunnable<Void>() {
                        public Void run() throws ParseException {
                            finalBatch.batch();
                            return null;
                        }
                    }).run();
                    batch = new ParseBatch();
                    batchSize = 0;
                }
            }
        }
    }
}
