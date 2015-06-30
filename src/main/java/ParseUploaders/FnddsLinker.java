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
    /*public static void main(String[] args) {
        final Logger logger = Utils.initLogger();
        Parse.initialize(PrivateKeys.APPLICATION_ID, PrivateKeys.REST_API_KEY);
        logger.info("Parse Initialized");
        new ParseRunnable(logger, new LinkerRunnable("FoodCodeSubcodeLink", "MainFoodDesc", logger, "foodCode", "foodDescription", true)).run();
    }

    static class LinkerRunnable implements ParseRunnable.FlakyParseRunnable {
        private String className;
        private Logger logger;
        private String fromClassName;
        private String linkField;
        private String field;
        private boolean reverse;

        public LinkerRunnable(String toClassName, String fromClassName, Logger logger, String linkField, String field, boolean reverse) {
            this.className = toClassName;
            this.logger = logger;
            this.fromClassName = fromClassName;
            this.linkField = linkField;
            this.field = field;
            this.reverse = reverse;
        }

        public void run() throws ParseException {
            final LinkBatch linkBatch = new LinkBatch();
            linkBatch.batch = new ParseBatch();
            linkBatch.batchSize = 0;
            linkBatch.reverse = reverse;
            if (!className.equals(fromClassName)) {
                ParseQuery<ParseObject> linkToQuery = ParseQuery.getQuery(className);
                ParseQuery<ParseObject> linkFromQuery = ParseQuery.getQuery(fromClassName);
                int linkToCount = linkToQuery.count();
                for (int linkToSkip = 0; linkToSkip < linkToCount;) {
                    linkToQuery.skip(linkToSkip);
                    linkBatch.skipTo = linkToSkip;
                    new ParseRunnable(logger, new LinkToRunnable(linkToCount, logger, linkToQuery, linkFromQuery, linkField, field, linkBatch)).run();
                    linkToSkip = linkBatch.skipTo;
                }
            }
            new ParseRunnable(logger, new ParseRunnable.FlakyParseRunnable() {
                public void run() throws ParseException {
                    linkBatch.batch.batch();
                }
            }).run();
        }
    }

    private static class LinkToRunnable implements ParseRunnable.FlakyParseRunnable {
        private final Logger logger;
        private final int linkToCount;
        private ParseQuery<ParseObject> linkToQuery;
        private ParseQuery<ParseObject> linkFromQuery;
        private String linkField;
        private String field;
        private LinkBatch linkBatch;

        public LinkToRunnable(int linkToCount, Logger logger, ParseQuery<ParseObject> linkToQuery, ParseQuery<ParseObject> linkFromQuery, String linkField, String field, LinkBatch linkBatch) {
            this.linkToQuery = linkToQuery;
            this.linkFromQuery = linkFromQuery;
            this.logger = logger;
            this.linkToCount = linkToCount;
            this.linkField = linkField;
            this.field = field;
            this.linkBatch = linkBatch;
        }

        public void run() throws ParseException {
                final List<ParseObject> linkFromParseObjects = linkToQuery.find();
                int linkToSize = linkFromParseObjects.size();
                linkBatch.skipTo += linkToSize;
                for (int n = 0; n < linkToSize; n++) {
                    final float progress = (float) (linkBatch.skipTo - linkToSize + n) * 100f / linkToCount;
                    logger.info(progress + "% done.");
                    new ParseRunnable(logger, new LinkFromRunnable(linkFromQuery, logger, linkField, field, linkBatch, linkFromParseObjects.get(n))).run();
                }
        }

    }

    private static class LinkFromRunnable implements ParseRunnable.FlakyParseRunnable {
        private final Logger logger;
        private ParseQuery<ParseObject> linkFromQuery;
        private String linkField;
        private String field;
        private LinkBatch linkBatch;
        private ParseObject linkToParseObject;

        public LinkFromRunnable(ParseQuery<ParseObject> linkFromQuery, Logger logger, String linkField, String field, LinkBatch linkBatch, ParseObject linkToParseObject) {
            this.linkFromQuery = linkFromQuery;
            this.logger = logger;
            this.linkField = linkField;
            this.field = field;
            this.linkBatch = linkBatch;
            this.linkToParseObject = linkToParseObject;
        }

        public void run() throws ParseException {
            long foodCode = linkToParseObject.getLong(linkField);
            linkFromQuery.whereEqualTo(linkField, foodCode);
            final int count = linkFromQuery.count();
            for (int skip = 0; skip < count; ) {
                linkBatch.skipFrom = skip;
                linkFromQuery.skip(skip);
                new ParseRunnable(logger, new LinkBatchRunnable(linkFromQuery, field, linkBatch, linkToParseObject)).run();
                skip = linkBatch.skipFrom;
            }

        }
    }

    private static class LinkBatchRunnable implements ParseRunnable.FlakyParseRunnable {
        private final ParseQuery<ParseObject> linkFromQuery;
        private final LinkBatch linkBatch;
        private final ParseObject linkToParseObject;
        private String field;

        public LinkBatchRunnable(ParseQuery<ParseObject> linkFromQuery, String field, LinkBatch linkBatch, ParseObject linkToParseObject) {
            this.linkFromQuery = linkFromQuery;
            this.linkBatch = linkBatch;
            this.field = field;
            this.linkToParseObject = linkToParseObject;
        }

        public void run() throws ParseException {
            final List<ParseObject> linkFromParseObjects = linkFromQuery.find();
            final int linkFromObjectCount = linkFromParseObjects.size();
            linkBatch.skipFrom += linkFromObjectCount;
            for (int i = 0; i < linkFromObjectCount; i++) {
                if (linkBatch.batchSize == 50) {
                    linkBatch.batch.batch();
                    linkBatch.batch = new ParseBatch();
                    linkBatch.batchSize = 0;
                }
                ParseObject linkFromParseObject = linkFromParseObjects.get(i);
                if (linkBatch.reverse) {
                    linkToParseObject.put(field, linkFromParseObject);
                    linkBatch.batch.updateObject(linkToParseObject);
                } else {
                    linkFromParseObject.put(field, linkToParseObject);
                    linkBatch.batch.updateObject(linkFromParseObject);
                }
                linkBatch.batchSize++;
            }
        }

    }

    public static class LinkBatch {
        int skipTo;
        int skipFrom;
        int batchSize;
        ParseBatch batch;
        boolean reverse;
    }*/
}
