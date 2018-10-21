package jp.hiro116s.cook.crawler.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class RecipeTest {
    @Test
    public void serdes() throws Exception {
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
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        final String json = objectMapper.writeValueAsString(testData);
        final Recipe actualData = objectMapper.readValue(json, ImmutableRecipe.class);
        assertEquals(testData, actualData);
    }
}