package no.toreb.nrknewsconsumer.model;

import java.util.Arrays;

public enum SortOrder {
    ASC, DESC;

    public static SortOrder fromValue(final String value) {
        return Arrays.stream(values())
                     .filter(it -> it.name().equalsIgnoreCase(value))
                     .findFirst()
                     .orElse(null);
    }
}
