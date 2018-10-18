package jp.hiro116s.cook.crawler.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Ingredient {
    public abstract String title();

    public abstract String amount();

    public static Ingredient create(final String title, final String amount) {
        return ImmutableIngredient.builder()
                .amount(amount)
                .title(title)
                .build();
    }
}
