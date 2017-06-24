package de.mineformers.investiture.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Provides utilities to do with item stacks, e.g. dropping them to the ground.
 */
public class ItemStacks
{
    private static final Random RANDOM = new Random();

    public static ItemStack from(@Nonnull final IBlockState state)
    {
        final Block block = state.getBlock();

        final Item i = block.getItemDropped(state, RANDOM, 0);
        final int meta = block.getMetaFromState(state);
        final int damage = block.damageDropped(state);
        final int amount = block.quantityDropped(state, 0, RANDOM);
        final Item variant = Item.getItemFromBlock(block);

        // darn conversions...
        if (block == Blocks.GRASS)
        {
            return new ItemStack(Blocks.GRASS);
        }

        if (i == null || variant == null || variant != i)
        {
            return ItemStack.EMPTY;
        }

        return new ItemStack(i, amount, damage);
    }

    /**
     * Spawns an item stack at a block's position.
     *
     * @param world the block's world
     * @param pos   the block's position
     * @param stack the stack to spawn
     */
    public static void spawn(World world, BlockPos pos, ItemStack stack)
    {
        // Spawn in the [0.1..0.9] bounds of the block
        float x = RANDOM.nextFloat() * 0.8F + 0.1F;
        float y = RANDOM.nextFloat() * 0.8F + 0.1F;
        float z = RANDOM.nextFloat() * 0.8F + 0.1F;

        // Split the stack iteratively into smaller ones and spawn each individual one, leading to a nicer distribution.
        while (stack.getCount() > 0)
        {
            int droppedCount = RANDOM.nextInt(21) + 10;

            if (droppedCount > stack.getCount())
            {
                droppedCount = stack.getCount();
            }

            EntityItem entity = new EntityItem(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                                               new ItemStack(stack.getItem(), droppedCount, stack.getMetadata()));
            if (stack.hasTagCompound())
            {
                entity.getItem().setTagCompound(stack.getTagCompound().copy());
            }
            stack.shrink(droppedCount);

            entity.motionX = RANDOM.nextGaussian() * 0.05;
            entity.motionY = RANDOM.nextGaussian() * 0.05 + 0.2;
            entity.motionZ = RANDOM.nextGaussian() * 0.05;
            world.spawnEntity(entity);
        }
    }
}
