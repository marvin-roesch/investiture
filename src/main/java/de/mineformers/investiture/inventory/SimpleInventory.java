package de.mineformers.investiture.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Simple inventory implementation that does most things for the implementer.
 */
public interface SimpleInventory extends IInventory
{
    @Override
    default ItemStack decrStackSize(int index, int count)
    {
        ItemStack stack = getStackInSlot(index);
        ItemStack returnStack = null;
        if (stack != null)
        {
            if (stack.stackSize <= count)
            {
                returnStack = stack;
                setInventorySlotContents(index, null);
            }
            else
            {
                returnStack = stack.splitStack(count);

                if (stack.stackSize == 0)
                {
                    setInventorySlotContents(index, null);
                }
                else
                {
                    markDirty();
                }
            }
        }
        return returnStack;
    }

    @Override
    default ItemStack removeStackFromSlot(int index)
    {
        ItemStack stack = this.getStackInSlot(index);
        setInventorySlotContents(index, null);
        return stack;
    }

    default void moveStack(int source, int destination)
    {
        if (getStackInSlot(source) != null && getStackInSlot(destination) == null)
        {
            setInventorySlotContents(destination, getStackInSlot(source));
            setInventorySlotContents(source, null);
        }
    }

    default void swapStacks(int slot1, int slot2)
    {
        ItemStack stack1 = getStackInSlot(slot1);
        ItemStack stack2 = getStackInSlot(slot2);
        setInventorySlotContents(slot1, stack2);
        setInventorySlotContents(slot2, stack1);
    }

    @Override
    default void clear()
    {
        for (int i = 0; i < getSizeInventory(); i++)
            setInventorySlotContents(i, null);
    }

    @Override
    default int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    default boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    default void writeInventoryToNBT(NBTTagCompound compound)
    {
        writeInventoryToNBT(compound, "Inventory");
    }

    default void writeInventoryToNBT(NBTTagCompound compound, String tagName)
    {
        Inventories.write(this, compound, tagName);
    }

    default void readInventoryFromNBT(NBTTagCompound compound)
    {
        readInventoryFromNBT(compound, "Inventory");
    }

    default void readInventoryFromNBT(NBTTagCompound compound, String tagName)
    {
        Inventories.read(this, compound, tagName);
    }
}
