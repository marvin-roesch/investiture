package de.mineformers.investiture.allomancy.api;

import de.mineformers.investiture.allomancy.api.misting.Misting;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;

/**
 * An Allomancer is capable of 'burning' metals and gaining powers through this process.
 * This interface provides access to the individual abilities of an Allomancer.
 */
@ParametersAreNonnullByDefault
public interface Allomancer
{
    /**
     * Checks if the Allomancer has the given ability.
     *
     * @param type the class of the ability to check
     * @return true if the Allomancer has the ability, false otherwise
     */
    default boolean is(Class<? extends Misting> type)
    {
        return as(type).isPresent();
    }

    /**
     * 'Converts' this instance into the representation of the given ability.
     * This grants access to the individual ability's properties.
     * Note that the method will not check for the exact type but the easiest match.
     * This means that passing <code>Misting.class</code> to this method will return a random ability.
     *
     * @param type the class of the ability to get
     * @return a present optional if this Allomancer has the ability, <code>Optional.empty()</code> otherwise
     */
    @Nonnull
    default <T extends Misting> Optional<T> as(Class<T> type)
    {
        return powers().stream().filter(m -> m.getClass().isAssignableFrom(type)).map(type::cast).findFirst();
    }

    /**
     * @return a view of all powers this Allomancer has access to
     */
    @Nonnull
    Collection<Misting> powers();
}
