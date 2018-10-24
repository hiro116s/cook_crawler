package jp.hiro116s.cook.crawler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mongodb.MongoClient;
import jp.hiro116s.cook.crawler.dao.MongoRecipeDataDao;
import jp.hiro116s.cook.crawler.model.ImmutableIngredient;
import jp.hiro116s.cook.crawler.model.ImmutableRecipe;
import jp.hiro116s.cook.crawler.model.Ingredient;
import jp.hiro116s.cook.crawler.model.Recipe;
import jp.hiro116s.cook.crawler.tokenizer.MecabTokenizer;
import jp.hiro116s.cook.crawler.tokenizer.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IngredientNormalizerTool {
    public static void main(final String[] args) throws Exception {
        final JapaneseAnalyzer analyzer = new JapaneseAnalyzer(
                null, JapaneseTokenizer.Mode.NORMAL, JapaneseAnalyzer.getDefaultStopSet(), JapaneseAnalyzer.getDefaultStopTags());
        final MecabTokenizer mecabTokenizer = new MecabTokenizer(analyzer);

        final ClassLoader classLoader = IngredientNormalizerTool.class.getClassLoader();
        final ImmutableList.Builder<Pair<String, Result>> tokenizedFoodsBuilder = ImmutableList.builder();
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(classLoader.getResource("foods.txt").getFile()))) {
            String nextLine = bufferedReader.readLine();
            while (nextLine != null) {
                final List<Result> nextResults = mecabTokenizer.tokenize(new StringReader(nextLine));
                if (nextResults.size() == 0) {
                    System.out.println(nextLine + " is no result");
                } else if (nextResults.size() >= 2) {
                    System.out.println(nextLine + " is tokenized to several words: " + nextResults.stream().map(Result::getText).collect(Collectors.toList()));
                } else {
                    tokenizedFoodsBuilder.add(ImmutablePair.of(nextLine, nextResults.get(0)));
                }
                nextLine = bufferedReader.readLine();
            }
        }
        final List<Pair<String, Result>> tokenizedFoods = tokenizedFoodsBuilder.build();
        for (final Pair<String ,Result> pair : tokenizedFoods) {
            System.out.println(pair);
        }
        final Map<String, String> textByReading = tokenizedFoods.stream()
                .distinct()
                .collect(Collectors.toMap(p -> p.getValue().getReading(), Pair::getKey));

        final MongoClient mongoClient = new MongoClient("localhost:27017");
        final MongoRecipeDataDao recipeDataDao = MongoRecipeDataDao.create(mongoClient);
        final List<? extends Recipe> recipes = recipeDataDao.readRecipes(new Document());

        final ImmutableList.Builder<Recipe> normalizedRecipesBuilder = ImmutableList.builder();
        for (final Recipe recipe : recipes) {
            final ImmutableList.Builder<Ingredient> normalizedIngredientsBuilder = ImmutableList.builder();
            for (final Ingredient ingredient : recipe.ingredients()) {
                final ImmutableList.Builder<String> builder = ImmutableList.builder();
                for (final Result result : mecabTokenizer.tokenize(new StringReader(ingredient.title()))) {
                    if (result.getReading() != null && textByReading.containsKey(result.getReading())) {
                        builder.add(result.getReading());
                    }
                }
                final ImmutableList<String> readings = builder.build();
                if (readings.isEmpty()) {
                    continue;
                }
                if (readings.size() >= 2) {
                    System.out.println("Some readings found for " + ingredient.title() + " : " + readings);
                    continue;
                }
                normalizedIngredientsBuilder.add(ImmutableIngredient.builder()
                        .title(textByReading.get(Iterables.getFirst(readings, ingredient.title())))
                        .amount(ingredient.amount())
                        .build());
            }
            final List<Ingredient> normalizedIngredients = normalizedIngredientsBuilder.build();
            if (normalizedIngredients.size() == recipe.ingredients().size()) {
                normalizedRecipesBuilder.add(ImmutableRecipe.builder()
                        .internalId(recipe.internalId())
                        .externalId(recipe.externalId())
                        .imageUrlBySizeType(recipe.imageUrlBySizeType())
                        .owner(recipe.owner())
                        .recipeSource(recipe.recipeSource())
                        .title(recipe.title())
                        .url(recipe.url())
                        .addAllIngredients(normalizedIngredients)
                        .build());
            }
        }
        final List<Recipe> normalizedRecipes = normalizedRecipesBuilder.build();
        System.out.println(normalizedRecipes.size());
        recipeDataDao.insertRecipes("normalizedRecipe", normalizedRecipes);
    }
}
