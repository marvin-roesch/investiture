package de.mineformers.investiture.allomancy.api.metal;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

public interface MetalMapping
{
    boolean matches(@Nonnull ItemStack stack);

    @Nonnull
    Metal getMetal(@Nonnull ItemStack stack);

    float getQuantity(@Nonnull ItemStack stack);

    float getPurity(@Nonnull ItemStack stack);

    abstract class Abstract implements MetalMapping
    {
        protected final Metal metal;
        protected final float conversionRate;
        protected final float purity;
        protected final boolean nbt;

        public Abstract(Metal metal, float conversionRate, float purity, boolean nbt)
        {
            this.metal = metal;
            this.conversionRate = conversionRate;
            this.purity = purity;
            this.nbt = nbt;
        }

        @Nonnull
        @Override
        public Metal getMetal(@Nonnull ItemStack stack)
        {
            return metal;
        }

        @Override
        public float getQuantity(@Nonnull ItemStack stack)
        {
            return conversionRate * stack.getCount();
        }

        @Override
        public float getPurity(@Nonnull ItemStack stack)
        {
            return purity;
        }
    }

    class Item extends Abstract
    {
        protected final ItemStack stack;

        public Item(ItemStack stack, Metal metal, float conversionRate, float purity, boolean nbt)
        {
            super(metal, conversionRate, purity, nbt);
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

    class OreDict extends Abstract
    {
        protected final String oreName;

        public OreDict(String oreName, Metal metal, float conversionRate, float purity, boolean nbt)
        {
            super(metal, conversionRate, purity, nbt);
            this.oreName = oreName;
        }

        @Override
        public boolean matches(@Nonnull ItemStack stack)
        {
            return OreDictionary.containsMatch(this.nbt, OreDictionary.getOres(this.oreName), stack);
        }
    }
}
