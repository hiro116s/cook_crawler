package jp.hiro116s.cook.crawler.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableIngredient.class)
@JsonDeserialize(as = ImmutableIngredient.class)
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
