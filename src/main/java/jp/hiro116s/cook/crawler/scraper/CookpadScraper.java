package jp.hiro116s.cook.crawler.scraper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jp.hiro116s.cook.crawler.model.ImageSizeType;
import jp.hiro116s.cook.crawler.model.ImmutableRecipe;
import jp.hiro116s.cook.crawler.model.ImmutableRecipeCategory;
import jp.hiro116s.cook.crawler.model.ImmutableUser;
import jp.hiro116s.cook.crawler.model.Ingredient;
import jp.hiro116s.cook.crawler.model.Recipe;
import jp.hiro116s.cook.crawler.model.RecipeCategory;
import jp.hiro116s.cook.crawler.model.RecipeSource;
import jp.hiro116s.cook.crawler.model.User;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class CookpadScraper implements IScraper {
    @Override
    public Recipe extractRecipe(final Document document, final URL url) {
        return ImmutableRecipe.builder()
                .externalId(extractExternalId(url))
                .title(extractRecipeTitle(document))
                .url(url)
                .recipeSource(RecipeSource.COOK_PAD)
                .owner(extractOwner(document))
                .putAllImageUrlBySizeType(extractImageUrls(document))
                .addAllIngredients(extractIngredients(document))
                .build();
    }

    @Override
    public RecipeCategory extractCategory(Document document, URL url) {
        return ImmutableRecipeCategory.builder()
                .externalId(extractExternalId(url))
                .title(extractCategoryTitle(document))
                .url(url)
                .recipeSource(RecipeSource.COOK_PAD)
                .build();
    }

    private int extractExternalId(final URL url) {
        final String[] files = url.getFile().split("/");
        return Integer.valueOf(files[files.length - 1]);
    }

    private String extractRecipeTitle(final Document document) {
        return document.selectFirst("#recipe-title .recipe-title").html();
    }

    private String extractCategoryTitle(final Document document) {
        return document.selectFirst("#nt_category_description .category_title").html();
    }

    private Iterable<? extends Ingredient> extractIngredients(final Document document) {
        final ImmutableList.Builder<Ingredient> builder = ImmutableList.builder();
        final Elements ingredients = document.select(".ingredient_row");
        for (final Element element : ingredients) {
            final Element titleElement = element.selectFirst(".ingredient_name span");
            final Element amountElement = element.selectFirst(".ingredient_quantity");
            if (amountElement != null) {
                final String title = titleElement.select("a").isEmpty() ? titleElement.html() : titleElement.selectFirst("a").html();
                builder.add(Ingredient.create(title, element.selectFirst(".ingredient_quantity").html()));
            }
        }
        return builder.build();
    }

    private Map<ImageSizeType, URL> extractImageUrls(final Document document) {
        try {
            return ImmutableMap.<ImageSizeType, URL>builder()
                    .put(ImageSizeType.T280x210, new URL(document.selectFirst("#main-photo img").attr("src")))
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private User extractOwner(final Document document) {
        final Element recipeAuthorName = document.selectFirst("#recipe_author_name");
        return ImmutableUser.builder()
                .externalId(Integer.valueOf(recipeAuthorName.attr("href").split("/")[2]))
                .title(recipeAuthorName.html())
                .url(toCookpadURL(recipeAuthorName.attr("href")))
                .build();
    }

    private URL toCookpadURL(final String filename) {
        try {
            return new URL("https", "cookpad.com", filename);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
