package jp.hiro116s.cook.crawler;

import com.google.common.collect.Multiset;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import jp.hiro116s.cook.crawler.dao.RecipeDataDao;
import jp.hiro116s.cook.crawler.scraper.CookpadScraper;
import jp.hiro116s.cook.crawler.scraper.IScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

public class CookpadCrawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile("^https://cookpad.com/(recipe|category)/[0-9]+");

    private final Multiset<String> visitedUrlSet;
    private final RecipeDataDao recipeDataDao;

    public CookpadCrawler(Multiset<String> visitedUrlSet, RecipeDataDao recipeDataDao) {
        this.visitedUrlSet = visitedUrlSet;
        this.recipeDataDao = recipeDataDao;
    }

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        final String rawUrl = url.getURL();
        if (FILTERS.matcher(rawUrl).matches() && !visitedUrlSet.contains(rawUrl)) {
            System.out.println("Added : " + rawUrl);
            visitedUrlSet.add(rawUrl);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(final Page page) {
        if (!(page.getParseData() instanceof HtmlParseData)) {
            throw new IllegalPageFormatException("Page format is not HTML");
        }
        final HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
        final Document document = Jsoup.parse(htmlParseData.getHtml());

        final IScraper scraper = new CookpadScraper();
        final String url = page.getWebURL().getURL();
        try {
            if (url.matches(".*/recipe/[0-9]+")) {
                recipeDataDao.insertRecipe(scraper.extractRecipe(document, new URL(url)));
            } else if (url.matches(".*/category/[0-9]+")) {
                recipeDataDao.insertRecipeCategory(scraper.extractCategory(document, new URL(url)));
            } else {
                System.out.println(url + " : no parse");
            }
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private class IllegalPageFormatException extends RuntimeException {
        private IllegalPageFormatException(String message) {
            super(message);
        }
    }
}
