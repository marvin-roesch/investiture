package de.mineformers.investiture.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Provides utility methods both for reading from and writing to inventories.
 */
@ParametersAreNonnullByDefault
public class Inventories
{
    public static boolean insert(IInventory inventory, ItemStack stack, int index, EnumFacing side)
    {
        boolean result = false;
        ItemStack stored = inventory.getStackInSlot(index);

        if (canInsertItemInSlot(inventory, stack, index, side))
        {
            boolean shouldUpdate = false;

            int max = Math.min(stack.getMaxStackSize(), inventory.getInventoryStackLimit());
            if (stored == null)
            {
                if (max >= stack.stackSize)
                {
                    inventory.setInventorySlotContents(index, stack);
                    result = true;
                }
                else
                {
                    inventory.setInventorySlotContents(index, stack.splitStack(max));
                }
                shouldUpdate = true;
            }
            else if (canCombine(stored, stack))
            {
                if (max > stored.stackSize)
                {
                    int storedAmount = Math.min(stack.stackSize, max - stored.stackSize);
                    stack.stackSize -= storedAmount;
                    stored.stackSize += storedAmount;
                    shouldUpdate = storedAmount > 0;
                }
            }

            if (shouldUpdate)
            {
                inventory.markDirty();
            }
        }

        return result;
    }

    public static boolean canInsertItemInSlot(IInventory inventory, ItemStack stack, int index, EnumFacing side)
    {
        return inventory.isItemValidForSlot(index, stack) &&
            (!(inventory instanceof ISidedInventory) || ((ISidedInventory) inventory).canInsertItem(index, stack, side));
    }

    public static boolean canCombine(ItemStack stack1, ItemStack stack2)
    {
        return stack1.getItem() == stack2.getItem() &&
            stack1.getMetadata() == stack2.getMetadata() &&
            stack1.stackSize <= stack1.getMaxStackSize() &&
            ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    /**
     * Write an inventory to an NBT tag compound, storing it in a list with the given name.
     *
     * @param inventory the inventory to write
     * @param compound  the tag compound the list should be stored in
     * @param tagName   the name of the list
     */
    public static void write(IInventory inventory, NBTTagCompound compound, String tagName)
    {
        compound.setTag(tagName, write(inventory));
    }

    /**
     * Write an inventory to an NBT tag list for persistence.
     *
     * @param inventory the inventory to write
     * @return a list of tags representing each (non-empty) slot in the inventory
     */
    public static NBTTagList write(IInventory inventory)
    {
        NBTTagList nbt = new NBTTagList();
        int length = inventory.getSizeInventory();
        for (int i = 0; i < length; i++)
        {
            ItemStack item = inventory.getStackInSlot(i);
            if (item != null)
            {
                NBTTagCompound itemCompound = item.writeToNBT(new NBTTagCompound());
                itemCompound.setInteger("slot", i);
                nbt.appendTag(itemCompound);
            }
        }
        return nbt;
    }

    /**
     * Read inventory contents from an NBT tag list stored in a tag compound and store them in the inventory.
     *
     * @param inventory the inventory to store the contents in
     * @param compound  the tag compound to get the contents from
     * @param tagName   the name of the tag list storing the contents
     */
    public static void read(IInventory inventory, NBTTagCompound compound, String tagName)
    {
        read(inventory, compound.getTagList(tagName, Constants.NBT.TAG_COMPOUND));
    }

    /**
     * Read inventory contents from an NBT tag list and store them in the inventory.
     *
     * @param inventory the inventory to store the contents in
     * @param list      the list of inventory contents
     */
    public static void read(IInventory inventory, NBTTagList list)
    {
        int inventorySize = inventory.getSizeInventory();
        int listSize = list.tagCount();
        for (int i = 0; i < listSize; i++)
        {
            NBTTagCompound stackCompound = list.getCompoundTagAt(i);

            ItemStack stack = ItemStack.loadItemStackFromNBT(stackCompound);
            int slot = stackCompound.getInteger("slot");
            if (slot >= 0 && slot < inventorySize)
                inventory.setInventorySlotContents(slot, stack);
        }
    }
}
