package jp.hiro116s.cook.crawler.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.fakemongo.junit.FongoRule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClient;
import jp.hiro116s.cook.crawler.model.ImageSizeType;
import jp.hiro116s.cook.crawler.model.ImmutableRecipe;
import jp.hiro116s.cook.crawler.model.ImmutableRecipeCategory;
import jp.hiro116s.cook.crawler.model.ImmutableUser;
import jp.hiro116s.cook.crawler.model.Ingredient;
import jp.hiro116s.cook.crawler.model.Recipe;
import jp.hiro116s.cook.crawler.model.RecipeCategory;
import jp.hiro116s.cook.crawler.model.RecipeSource;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static jp.hiro116s.cook.crawler.dao.MongoRecipeDataDao.DB_NAME;
import static org.junit.Assert.assertEquals;

public class MongoRecipeDataDaoTest {
    private ObjectMapper objectMapper;

    private MongoClient mongoClient;

    private MongoRecipeDataDao mongoRecipeDataDao;

    @ClassRule
    final public static FongoRule fongoRule = new FongoRule();

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        mongoClient = fongoRule.getMongoClient();
        mongoRecipeDataDao = MongoRecipeDataDao.create(mongoClient);
    }

    @After
    public void teardown() {
        fongoRule.getMongoClient().getDatabase(DB_NAME).drop();
    }

    @Test
    public void testInsertRecipeCategory() throws Exception {
        final URL categoryUrl = new URL("https://cookpad.com/category/1232");
        final RecipeCategory testData = ImmutableRecipeCategory.builder()
                .externalId(1232)
                .title("うなぎ")
                .url(categoryUrl)
                .recipeSource(RecipeSource.COOK_PAD)
                .build();
        mongoRecipeDataDao.insertRecipeCategories(ImmutableList.of(testData));
        final List<RecipeCategory> actual = StreamSupport.stream(mongoClient.getDatabase(DB_NAME).getCollection("category").find().spliterator(), false)
                .map(document -> {
                    try {
                        document.remove("_id");
                        return objectMapper.readValue(document.toJson(), ImmutableRecipeCategory.class);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        final List<RecipeCategory> expected = ImmutableList.of(testData);
        assertEquals(expected, actual);
    }

    @Test
    public void testInsertRecipe() throws Exception {
        final URL recipeUrl = new URL("https://cookpad.com/recipe/854214");
        final Recipe testData = ImmutableRecipe.builder()
                .externalId(854214)
                .title("あっというまに♪うなぎの柳川風")
                .url(recipeUrl)
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
                .build();
        mongoRecipeDataDao.insertRecipes(ImmutableList.of(testData));
        final List<Recipe> actual = StreamSupport.stream(mongoClient.getDatabase(DB_NAME).getCollection("recipe").find().spliterator(), false)
                .map(document -> {
                    try {
                        document.remove("_id");
                        return objectMapper.readValue(document.toJson(), ImmutableRecipe.class);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        final List<Recipe> expected = ImmutableList.of(testData);
        assertEquals(expected, actual);
    }
}