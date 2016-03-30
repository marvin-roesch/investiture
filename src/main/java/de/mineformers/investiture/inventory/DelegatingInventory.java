package de.mineformers.investiture.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

/**
 * Provides simple functionality for inventories which delegate all their calls to another one.
 */
public interface DelegatingInventory extends IInventory
{
    @Nonnull
    IInventory getDelegate();

    @Override
    default int getSizeInventory()
    {
        return getDelegate().getSizeInventory();
    }

    @Override
    default ItemStack getStackInSlot(int index)
    {
        return getDelegate().getStackInSlot(index);
    }

    @Override
    default ItemStack decrStackSize(int index, int count)
    {
        return getDelegate().decrStackSize(index, count);
    }

    @Override
    default ItemStack removeStackFromSlot(int index)
    {
        return getDelegate().removeStackFromSlot(index);
    }

    @Override
    default void setInventorySlotContents(int index, ItemStack stack)
    {
        getDelegate().setInventorySlotContents(index, stack);
    }

    @Override
    default int getInventoryStackLimit()
    {
        return getDelegate().getInventoryStackLimit();
    }

    @Override
    default void markDirty()
    {
        getDelegate().markDirty();
    }

    @Override
    default boolean isUseableByPlayer(EntityPlayer player)
    {
        return getDelegate().isUseableByPlayer(player);
    }

    @Override
    default void openInventory(EntityPlayer player)
    {
        getDelegate().openInventory(player);
    }

    @Override
    default void closeInventory(EntityPlayer player)
    {
        getDelegate().closeInventory(player);
    }

    @Override
    default boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return getDelegate().isItemValidForSlot(index, stack);
    }

    @Override
    default int getField(int id)
    {
        return getDelegate().getField(id);
    }

    @Override
    default void setField(int id, int value)
    {
        getDelegate().setField(id, value);
    }

    @Override
    default int getFieldCount()
    {
        return getDelegate().getFieldCount();
    }

    @Override
    default void clear()
    {
        getDelegate().clear();
    }

    @Override
    default String getName()
    {
        return getDelegate().getName();
    }

    @Override
    default boolean hasCustomName()
    {
        return getDelegate().hasCustomName();
    }

    @Override
    default ITextComponent getDisplayName()
    {
        return getDelegate().getDisplayName();
    }
}
