package jp.hiro116s.cook.crawler.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.annotations.VisibleForTesting;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import jp.hiro116s.cook.crawler.model.Recipe;
import jp.hiro116s.cook.crawler.model.RecipeCategory;
import org.bson.Document;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MongoRecipeDataDao implements RecipeDataDao {
    @VisibleForTesting
    static final String DB_NAME = "db";

    private final MongoDatabase mongoDatabase;
    private final ObjectMapper objectMapper;

    public MongoRecipeDataDao(final MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
    }

    public static MongoRecipeDataDao create(final MongoClient mongoClient) {
        return new MongoRecipeDataDao(mongoClient.getDatabase(DB_NAME));
    }

    @Override
    public void insertRecipes(final Iterable<Recipe> recipes) {
        insertInternal("recipe", recipes);
    }

    @Override
    public void insertRecipeCategories(final Iterable<RecipeCategory> recipeCategory) {
        insertInternal("category", recipeCategory);
    }

    private void insertInternal(final String collectionName, final Iterable<? extends Object> jsonSerializableObjects) {
        mongoDatabase.getCollection(collectionName).insertMany(StreamSupport.stream(jsonSerializableObjects.spliterator(), false)
                .map(obj -> {
                    try {
                        return Document.parse(objectMapper.writeValueAsString(obj));
                    } catch (final JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList()));
    }
}
