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
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static jp.hiro116s.cook.crawler.dao.MongoRecipeDataDao.DB_NAME;
import static org.junit.Assert.assertEquals;

public class MongoRecipeDataDaoTest {
    private static final URL RECIPE_URL;
    private static final URL CATEGORY_URL;
    private static final Recipe RECIPE_DATA;

    static {
        try {
            RECIPE_URL = new URL("https://cookpad.com/recipe/854214");
            CATEGORY_URL = new URL("https://cookpad.com/category/1232");
            RECIPE_DATA = ImmutableRecipe.builder()
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
                    .build();
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final RecipeCategory CATEGORY_DATA = ImmutableRecipeCategory.builder()
            .externalId(1232)
            .title("うなぎ")
            .url(CATEGORY_URL)
            .recipeSource(RecipeSource.COOK_PAD)
            .build();

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
    public void testInsertNotDuplicated() {
        mongoRecipeDataDao.insertRecipeCategories(ImmutableList.of(CATEGORY_DATA));
        mongoRecipeDataDao.insertRecipeCategories(ImmutableList.of(CATEGORY_DATA));
        final List<RecipeCategory> actual = StreamSupport.stream(mongoClient.getDatabase(DB_NAME).getCollection("category").find().spliterator(), false)
                .map(document -> {
                    try {
                        document.remove("_id");
                        return objectMapper.readValue(document.toJson(), ImmutableRecipeCategory.class);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        final List<RecipeCategory> expected = ImmutableList.of(CATEGORY_DATA);
        assertEquals(expected, actual);

    }

    @Test
    public void testInsertRecipeCategory() {
        mongoRecipeDataDao.insertRecipeCategories(ImmutableList.of(CATEGORY_DATA));
        final List<RecipeCategory> actual = StreamSupport.stream(mongoClient.getDatabase(DB_NAME).getCollection("category").find().spliterator(), false)
                .map(document -> {
                    try {
                        document.remove("_id");
                        return objectMapper.readValue(document.toJson(), ImmutableRecipeCategory.class);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        final List<RecipeCategory> expected = ImmutableList.of(CATEGORY_DATA);
        assertEquals(expected, actual);
    }

    @Test
    public void testInsertRecipe() throws Exception {
        mongoRecipeDataDao.insertRecipes(ImmutableList.of(RECIPE_DATA));
        final List<Recipe> actual = StreamSupport.stream(mongoClient.getDatabase(DB_NAME).getCollection("recipe").find().spliterator(), false)
                .map(document -> {
                    try {
                        document.remove("_id");
                        return objectMapper.readValue(document.toJson(), ImmutableRecipe.class);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        final List<Recipe> expected = ImmutableList.of(RECIPE_DATA);
        assertEquals(expected, actual);
    }

    @Test
    public void testReadRecipes() throws Exception {
        mongoRecipeDataDao.insertRecipe(RECIPE_DATA);
        // Dummy
        mongoRecipeDataDao.insertRecipe(ImmutableRecipe.builder()
                .from(RECIPE_DATA)
                .externalId(1234)
                .build());
        final List<? extends Recipe> actual = mongoRecipeDataDao.readRecipes(new Document("externalId", RECIPE_DATA.externalId()));
        final List<? extends Recipe> expected = ImmutableList.of(RECIPE_DATA);
        assertEquals(expected, actual);
    }

    @Test
    public void testReadRecipeCategories() throws Exception {
        mongoRecipeDataDao.insertRecipeCategory(CATEGORY_DATA);
        // Dummy
        mongoRecipeDataDao.insertRecipeCategory(ImmutableRecipeCategory.builder()
                .from(CATEGORY_DATA)
                .externalId(3456)
                .build());
        final List<? extends RecipeCategory> actual = mongoRecipeDataDao.readRecipeCategories(new Document("externalId", CATEGORY_DATA.externalId()));
        final List<? extends RecipeCategory> expected = ImmutableList.of(CATEGORY_DATA);
        assertEquals(expected, actual);
    }
}