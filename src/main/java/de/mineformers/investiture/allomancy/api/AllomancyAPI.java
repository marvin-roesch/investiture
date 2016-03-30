package de.mineformers.investiture.allomancy.api;

import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.impl.misting.temporal.SpeedBubble;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

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

    <T> void registerEquality(Class<T> type, BiPredicate<T, T> predicate);

    void registerMetallicItem(Predicate<ItemStack> predicate);

    void registerMetallicBlock(Predicate<BlockWorldState> predicate);

    void registerMetallicEntity(Predicate<Entity> predicate);

    default boolean isMetallic(ItemStack stack)
    {
        return metallicItems().stream().anyMatch(p -> p.test(stack));
    }

    default boolean isMetallic(World world, BlockPos pos)
    {
        BlockWorldState state = new BlockWorldState(world, pos, true);
        return metallicBlocks().stream().anyMatch(p -> p.test(state));
    }

    default boolean isMetallic(IBlockState state)
    {
        return metallicBlocks().stream().anyMatch(p -> p.test(new BlockWorldState(null, new BlockPos(0, 0, 0), true)
        {
            @Override
            public TileEntity getTileEntity()
            {
                return null;
            }

            @Override
            public IBlockState getBlockState()
            {
                return state;
            }
        }));
    }

    default boolean isMetallic(Entity entity)
    {
        return metallicEntities().stream().anyMatch(p -> p.test(entity));
    }

    @Nonnull
    Collection<Predicate<ItemStack>> metallicItems();

    @Nonnull
    Collection<Predicate<BlockWorldState>> metallicBlocks();

    @Nonnull
    Collection<Predicate<Entity>> metallicEntities();

    Iterable<SpeedBubble> speedBubbles(World world);
}
