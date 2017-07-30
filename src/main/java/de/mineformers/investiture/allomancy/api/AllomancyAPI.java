package de.mineformers.investiture.allomancy.api;

import com.google.common.collect.FluentIterable;
import de.mineformers.investiture.allomancy.api.metal.MetalMapping;
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
     * Registers an additional Misting type. This doesn't necessarily match the lore (yet),
     * but who knows what other metals will be useful allomantically?
     *
     * @param type    the base interface of the Misting type
     * @param factory a factory generating instances of the Misting type
     * @param <T>     the Misting type
     */
    <T extends Misting> void registerMisting(Class<T> type, MistingFactory<? extends T> factory);

    /**
     * Registers a custom equality check to be used by the automatic synchronisation of Mistings.
     *
     * @param type      the class the equality holds up for
     * @param predicate a predicate defining the actual equality relation
     * @param <T>       the type the equality holds up for
     */
    <T> void registerEquality(Class<T> type, BiPredicate<T, T> predicate);

    /**
     * Register an item which is to be considered metallic.
     *
     * @param predicate
     */
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
        world.profiler.startSection("investiture:metallic_check");
        boolean result = metallicBlocks().stream().anyMatch(p -> p.test(state));
        world.profiler.endSection();
        return result;
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

            @Nonnull
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

    Optional<MetalMapping> getMapping(ItemStack stack);

    void registerMetalMapping(MetalMapping mapping);
}
