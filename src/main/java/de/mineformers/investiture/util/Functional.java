package de.mineformers.investiture.util;

import java.util.Optional;

/**
 * Provides utility methods for work with various functional idioms.
 */
public class Functional
{
    public static <T> Optional<T> flatten(Optional<Optional<T>> optional)
    {
        if (optional.isPresent())
            return optional.get();
        else
            return Optional.empty();
    }

    public static <T> Optional<T> flatten(com.google.common.base.Optional<com.google.common.base.Optional<T>> optional)
    {
        if (optional.isPresent())
            return convert(optional.get());
        else
            return Optional.empty();
    }

    public static boolean isPresent(com.google.common.base.Optional<?> optional)
    {
        return optional.isPresent();
    }

    public static <T> Optional<T> convert(com.google.common.base.Optional<T> guavaOptional)
    {
        if (guavaOptional.isPresent())
            return Optional.of(guavaOptional.get());
        else
            return Optional.empty();
    }
}
