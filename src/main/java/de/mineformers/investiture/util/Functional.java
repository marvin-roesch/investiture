package de.mineformers.investiture.util;

import de.mineformers.investiture.allomancy.extractor.ExtractorOutput;

import java.util.Optional;

/**
 * Provides utilities for work with various functional idioms.
 */
public class Functional
{
    /**
     * Flattens an optional of another optional, yielding a single optional value.
     *
     * @param optional the optional to flatten
     * @return a flattened optional
     */
    public static <T> Optional<T> flatten(Optional<Optional<T>> optional)
    {
        if (optional.isPresent())
            return optional.get();
        else
            return Optional.empty();
    }

    /**
     * Converts from Guava's Optional to Java 8's.
     *
     * @param guavaOptional the Guava Optional
     * @return an equivalent Java 8 Optional
     */
    public static <T> Optional<T> convert(com.google.common.base.Optional<T> guavaOptional)
    {
        if (guavaOptional.isPresent())
            return Optional.of(guavaOptional.get());
        else
            return Optional.empty();
    }
}
