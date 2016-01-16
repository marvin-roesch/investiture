package de.mineformers.investiture.allomancy.tileentity;

import de.mineformers.investiture.inventory.DelegatingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

/**
 * Delegates inventory interactions to master
 */
public class TileMetalExtractorOutput extends TileMetalExtractorSlave implements DelegatingInventory, ISidedInventory
{
    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        if (getMaster() == null)
            return new int[0];
        return getMaster().getSlotsForFace(side);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction)
    {
        return getMaster().canInsertItem(index, stack, direction);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        return getMaster().canExtractItem(index, stack, direction);
    }

    @Nonnull
    @Override
    public IInventory getDelegate()
    {
        return getMaster();
    }
}
