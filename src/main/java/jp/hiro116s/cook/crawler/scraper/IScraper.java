package jp.hiro116s.cook.crawler.scraper;

import jp.hiro116s.cook.crawler.model.Recipe;
import jp.hiro116s.cook.crawler.model.RecipeCategory;
import org.jsoup.nodes.Document;

import java.net.URL;

public interface IScraper {
    Recipe extractRecipe(final Document document, final URL url);
    RecipeCategory extractCategory(final Document document, final URL url);
}
