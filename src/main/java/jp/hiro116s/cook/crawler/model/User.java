package jp.hiro116s.cook.crawler.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;

import java.net.URL;

@Value.Immutable
@JsonSerialize(as = ImmutableUser.class)
@JsonDeserialize(as = ImmutableUser.class)
public abstract class User {
    public abstract Optional<Integer> internalId();

    public abstract int externalId();

    public abstract String title();

    public abstract URL url();

    public static User create(final int internalId, final int externalId, final String title, final URL url) {
        return ImmutableUser.builder()
                .externalId(externalId)
                .internalId(internalId)
                .title(title)
                .url(url)
                .build();
    }
}
