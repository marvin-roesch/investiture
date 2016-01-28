package de.mineformers.investiture.allomancy.api;

import de.mineformers.investiture.allomancy.api.misting.Misting;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * Provides access to {@link Allomancer} instances of entities.
 * <p>
 * An instance of this interface can be acquired through IMC.
 */
@ParametersAreNonnullByDefault
public interface AllomancyAPI
{
    /**
     * Checks if the given entity is an Allomancer.
     *
     * @param entity the entity to check
     * @return true if the entity is in fact an Allomancer, false otherwise.
     */
    default boolean isAllomancer(Entity entity)
    {
        return toAllomancer(entity).isPresent();
    }

    /**
     * Tries to get hold of the {@link Allomancer} instance associated with the given entity.
     *
     * @param entity the entity to get the instance for
     * @return a present optional if the entity is an Allomancer, <code>Optional.empty()</code> otherwise
     */
    @Nonnull
    Optional<Allomancer> toAllomancer(Entity entity);

    <T extends Misting> void registerMisting(Class<T> type, MistingFactory<? extends T> factory);
}
