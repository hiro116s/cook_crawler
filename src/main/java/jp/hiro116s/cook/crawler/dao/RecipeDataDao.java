package jp.hiro116s.cook.crawler.dao;

import com.google.common.collect.ImmutableList;
import jp.hiro116s.cook.crawler.model.Recipe;
import jp.hiro116s.cook.crawler.model.RecipeCategory;
import org.bson.conversions.Bson;

public interface RecipeDataDao {
    void insertRecipes(final Iterable<Recipe> recipes);
    void insertRecipeCategories(final Iterable<RecipeCategory> recipeCategories);
    ImmutableList<? extends Recipe> readRecipes(final Bson filter);
    ImmutableList<? extends RecipeCategory> readRecipeCategories(final Bson filter);

    default void insertRecipe(final Recipe recipe) {
        insertRecipes(ImmutableList.of(recipe));
    }

    default void insertRecipeCategory(final RecipeCategory recipeCategory) {
        insertRecipeCategories(ImmutableList.of(recipeCategory));
    }
}
