package jp.hiro116s.cook.crawler.model;

import org.immutables.value.Value;

import java.net.URL;
import java.util.Optional;

@Value.Immutable
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
