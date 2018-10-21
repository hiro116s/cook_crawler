package jp.hiro116s.cook.crawler.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;

import java.net.URL;

@Value.Immutable
@JsonSerialize(as = ImmutableRecipeCategory.class)
@JsonDeserialize(as = ImmutableRecipeCategory.class)
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
