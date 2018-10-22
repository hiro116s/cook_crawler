package jp.hiro116s.cook.crawler.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import jp.hiro116s.cook.crawler.model.ImmutableRecipe;
import jp.hiro116s.cook.crawler.model.ImmutableRecipeCategory;
import jp.hiro116s.cook.crawler.model.Recipe;
import jp.hiro116s.cook.crawler.model.RecipeCategory;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.List;
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
    public void insertRecipeCategories(final Iterable<RecipeCategory> recipeCategories) {
        insertInternal("category", recipeCategories);
    }

    @Override
    public ImmutableList<? extends Recipe> readRecipes(final Bson filter) {
        return readInternal("recipe", ImmutableRecipe.class, filter);
    }

    @Override
    public ImmutableList<? extends RecipeCategory> readRecipeCategories(Bson filter) {
        return readInternal("category", ImmutableRecipeCategory.class, filter);
    }

    private <T> ImmutableList<? extends T> readInternal(final String collectionName, final Class<? extends T> clazz, final Bson filter) {
        return FluentIterable.from(mongoDatabase.getCollection(collectionName).find(filter))
                .transform(document -> {
                    try {
                        document.remove("_id");
                        return objectMapper.readValue(document.toJson(), clazz);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    private void insertInternal(final String collectionName, final Iterable<? extends Object> jsonSerializableObjects) {
        final List<Document> documents = StreamSupport.stream(jsonSerializableObjects.spliterator(), false)
                .map(obj -> {
                    try {
                        return Document.parse(objectMapper.writeValueAsString(obj));
                    } catch (final JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(document -> mongoDatabase.getCollection(collectionName).find(new Document("externalId", document.get("externalId"))).first() == null)
                .collect(Collectors.toList());

        if (!documents.isEmpty()) {
            mongoDatabase.getCollection(collectionName).insertMany(documents);
        }
    }
}
