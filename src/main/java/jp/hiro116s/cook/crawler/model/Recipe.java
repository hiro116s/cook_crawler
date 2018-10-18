package jp.hiro116s.cook.crawler.model;

import org.immutables.value.Value;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value.Immutable
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