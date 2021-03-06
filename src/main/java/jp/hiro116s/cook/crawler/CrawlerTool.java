package jp.hiro116s.cook.crawler;

import com.google.common.collect.ConcurrentHashMultiset;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import jp.hiro116s.cook.crawler.dao.MongoRecipeDataDao;
import jp.hiro116s.cook.crawler.dao.RecipeDataDao;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class CrawlerTool {
    public static void main(final String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","INFO");
        final Arguments arguments = parseArgs(args);

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(arguments.storagePath);
        config.setMaxDepthOfCrawling(10);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("https://cookpad.com/category/177");

        final MongoClient mongoClient = new MongoClient(new MongoClientURI(arguments.mongoDbUri));
        final RecipeDataDao recipeDataDao = MongoRecipeDataDao.create(mongoClient);
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(() -> new CookpadCrawler(
                ConcurrentHashMultiset.create(),
                recipeDataDao
        ), arguments.numberOfCrawlers);

        mongoClient.close();
    }

    private static Arguments parseArgs(final String[] args) {
        final Arguments arguments = new Arguments();
        final CmdLineParser parser = new CmdLineParser(arguments);
        try {
            parser.parseArgument(args);
        } catch (final CmdLineException e) {
            System.err.println(e);
            System.exit(1);
        }
        return arguments;
    }

    static class Arguments {
        @Option(name = "--numberOfCrawlers")
        private int numberOfCrawlers = 1;

        @Option(name = "--storagePath")
        private String storagePath;

        @Option(name = "--mongoDbUri")
        private String mongoDbUri = "mongodb://localhost:27017";
    }
}
