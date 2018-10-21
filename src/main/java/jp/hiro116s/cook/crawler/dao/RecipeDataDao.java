package jp.hiro116s.cook.crawler.dao;

import com.google.common.collect.ImmutableList;
import jp.hiro116s.cook.crawler.model.Recipe;
import jp.hiro116s.cook.crawler.model.RecipeCategory;

public interface RecipeDataDao {
    void insertRecipes(final Iterable<Recipe> recipes);
    void insertRecipeCategories(final Iterable<RecipeCategory> recipeCategories);

    default void insertRecipe(final Recipe recipe) {
        insertRecipes(ImmutableList.of(recipe));
    }

    default void insertRecipeCategory(final RecipeCategory recipeCategory) {
        insertRecipeCategories(ImmutableList.of(recipeCategory));
    }
}
