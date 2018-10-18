package jp.hiro116s.cook.crawler.model;

import org.immutables.value.Value;

import java.net.URL;
import java.util.Optional;

@Value.Immutable
public abstract class RecipeCategory {

    public abstract Optional<Integer> internalId();

    public abstract int externalId();

    public abstract String title();

    public abstract URL url();

    public abstract RecipeSource recipeSource();

    public static RecipeCategory create(final int internalId, final int externalId, final String title, final URL url, final RecipeSource recipeSource) {
        return ImmutableRecipeCategory.builder()
                .externalId(externalId)
                .internalId(internalId)
                .title(title)
                .url(url)
                .recipeSource(recipeSource)
                .build();
    }
}
