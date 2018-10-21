package jp.hiro116s.cook.crawler.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;

import java.net.URL;
import java.util.List;
import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableRecipe.class)
@JsonDeserialize(as = ImmutableRecipe.class)
public abstract class Recipe {
    public abstract Optional<Integer> internalId();

    public abstract int externalId();

    public abstract String title();

    public abstract URL url();

    public abstract RecipeSource recipeSource();

    public abstract User owner();

    public abstract Map<ImageSizeType, URL> imageUrlBySizeType();

    public abstract List<Ingredient> ingredients();
}