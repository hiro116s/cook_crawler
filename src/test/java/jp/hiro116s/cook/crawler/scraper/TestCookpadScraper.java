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
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class TestCookpadScraper {
    private static final Path RECIPE_HTML_FILE_PATH = Paths.get("cookpad/html", "recipe.html");
    private static final Path CATEGORY_HTML_FILE_PATH = Paths.get("cookpad/html", "category.html");
    private static final URL RECIPE_URL;
    private static final URL CATEGORY_URL;
    static {
        try {
            RECIPE_URL = new URL("https://cookpad.com/recipe/854214");
            CATEGORY_URL = new URL("https://cookpad.com/category/1232");
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document recipeDocument;
    private static Document categoryDocument;
    private static CookpadScraper cookpadScraper;

    @BeforeClass
    public static void beforeClass() throws Exception {
        final ClassLoader classLoader = TestCookpadScraper.class.getClassLoader();
        final File recipeFile = new File(classLoader.getResource(RECIPE_HTML_FILE_PATH.toString()).getFile());
        final File categoryFile = new File(classLoader.getResource(CATEGORY_HTML_FILE_PATH.toString()).getFile());
        recipeDocument = Jsoup.parse(FileUtils.readFileToString(recipeFile, StandardCharsets.UTF_8));
        categoryDocument = Jsoup.parse(FileUtils.readFileToString(categoryFile, StandardCharsets.UTF_8));
        cookpadScraper = new CookpadScraper();
    }

    @Test
    public void extractRecipe() throws Exception {
        final Recipe actual = cookpadScraper.extractRecipe(recipeDocument, RECIPE_URL);
        assertEquals(ImmutableRecipe.builder()
                .externalId(854214)
                .title("あっというまに♪うなぎの柳川風")
                .url(RECIPE_URL)
                .recipeSource(RecipeSource.COOK_PAD)
                .owner(ImmutableUser.builder()
                        .externalId(276760)
                        .title("yummysunny")
                        .url(new URL("https://cookpad.com/kitchen/276760"))
                        .build())
                .putAllImageUrlBySizeType(ImmutableMap.of(
                        ImageSizeType.T280x210, new URL("https://img.cpcdn.com/recipes/854214/280/a8b8ba1bbaf08c03cb4e849f7611fb82.jpg?u=276760&p=1246512137")))
                .addAllIngredients(ImmutableList.of(
                        Ingredient.create("うなぎ", "１切れ"),
                        Ingredient.create("ごぼう", "１／２本"),
                        Ingredient.create("卵", "３個"),
                        Ingredient.create("●水", "１／２カップ"),
                        Ingredient.create("●麺つゆ（三倍濃縮）", "大さじ３"),
                        Ingredient.create("●みりん", "大さじ１"),
                        Ingredient.create("酒", "大さじ１")))
                .build(), actual);
    }

    @Test
    public void extractCategory() {
        final RecipeCategory actual = cookpadScraper.extractCategory(categoryDocument, CATEGORY_URL);
        assertEquals(ImmutableRecipeCategory.builder()
                .externalId(1232)
                .title("うなぎ")
                .url(CATEGORY_URL)
                .recipeSource(RecipeSource.COOK_PAD)
                .build(), actual);
    }
}