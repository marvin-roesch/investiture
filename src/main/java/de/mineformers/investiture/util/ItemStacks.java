package de.mineformers.investiture.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Provides utility functions to do with item stacks, e.g. dropping them to the ground.
 */
public class ItemStacks
{
    private static final Random RANDOM = new Random();

    public static void spawn(World world, BlockPos pos, ItemStack stack)
    {
        float x = RANDOM.nextFloat() * 0.8F + 0.1F;
        float y = RANDOM.nextFloat() * 0.8F + 0.1F;
        float z = RANDOM.nextFloat() * 0.8F + 0.1F;

        while (stack.stackSize > 0)
        {
            int droppedCount = RANDOM.nextInt(21) + 10;

            if (droppedCount > stack.stackSize)
            {
                droppedCount = stack.stackSize;
            }

            stack.stackSize -= droppedCount;
            EntityItem entity = new EntityItem(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                                               new ItemStack(stack.getItem(), droppedCount, stack.getMetadata()));

            if (stack.hasTagCompound())
            {
                entity.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
            }

            entity.motionX = RANDOM.nextGaussian() * 0.05;
            entity.motionY = RANDOM.nextGaussian() * 0.05 + 0.2;
            entity.motionZ = RANDOM.nextGaussian() * 0.05;
            world.spawnEntityInWorld(entity);
        }
    }
}
