package de.mineformers.investiture.allomancy.api.metal;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

public interface MetalMapping extends MetalHolder<ItemStack>
{
    boolean matches(@Nonnull ItemStack stack);

    abstract class AbstractMetalMapping implements MetalMapping
    {
        protected final Metal metal;
        protected final float quantity;
        protected final boolean nbt;

        public AbstractMetalMapping(Metal metal, float quantity, boolean nbt)
        {
            this.metal = metal;
            this.quantity = quantity;
            this.nbt = nbt;
        }

        @Override
        public Metal getMetal(ItemStack stack)
        {
            return metal;
        }

        @Override
        public float getMetalQuantity(ItemStack stack)
        {
            return quantity * stack.getCount();
        }
    }

    class MetalMappingItem extends AbstractMetalMapping
    {
        protected final ItemStack stack;

        public MetalMappingItem(Metal metal, ItemStack stack, float quantity)
        {
            this(metal, stack, quantity, false);
        }

        public MetalMappingItem(Metal metal, ItemStack stack, float quantity, boolean nbt)
        {
            super(metal, quantity, nbt);
            this.stack = stack;
        }

        @Override
        public boolean matches(@Nonnull ItemStack stack)
        {
            if (this.nbt)
            {
                return ItemStack.areItemStacksEqual(stack, this.stack) && ItemStack.areItemStackTagsEqual(stack, this.stack);
            }

            return ItemStack.areItemStacksEqual(stack, this.stack);
        }
    }

    class MetalMappingOreDict extends AbstractMetalMapping
    {
        protected final String oreName;

        public MetalMappingOreDict(Metal metal, String oreName, float quantity, boolean nbt)
        {
            super(metal, quantity, nbt);
            this.oreName = oreName;
        }

        @Override
        public boolean matches(@Nonnull ItemStack stack)
        {
            return OreDictionary.containsMatch(this.nbt, OreDictionary.getOres(this.oreName), stack);
        }
    }
}
