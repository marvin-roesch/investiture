package de.mineformers.investiture.allomancy.api.metal;

import com.google.common.collect.Range;
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

    float getLowerPurityBound(@Nonnull ItemStack stack);

    float getUpperPurityBound(@Nonnull ItemStack stack);

    abstract class Abstract implements MetalMapping
    {
        protected final Metal metal;
        protected final float conversionRate;
        protected final float purity;
        protected final Range<Float> purityRange;
        protected final boolean nbt;

        public Abstract(Metal metal, float conversionRate, float purity, Range<Float> purityRange, boolean nbt)
        {
            this.metal = metal;
            this.conversionRate = conversionRate;
            this.purity = purity;
            this.purityRange = purityRange;
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
            return conversionRate;
        }

        @Override
        public float getPurity(@Nonnull ItemStack stack)
        {
            return purity;
        }

        @Override
        public float getLowerPurityBound(@Nonnull ItemStack stack)
        {
            return purityRange.hasLowerBound() ? purityRange.lowerEndpoint() : 0;
        }

        @Override
        public float getUpperPurityBound(@Nonnull ItemStack stack)
        {
            return purityRange.hasUpperBound() ? purityRange.upperEndpoint() : 1;
        }
    }

    class Item extends Abstract
    {
        protected final ItemStack stack;

        public Item(ItemStack stack, Metal metal, float conversionRate, float purity, Range<Float> purityRange, boolean nbt)
        {
            super(metal, conversionRate, purity, purityRange, nbt);
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

        public OreDict(String oreName, Metal metal, float conversionRate, float purity, Range<Float> purityRange, boolean nbt)
        {
            super(metal, conversionRate, purity, purityRange, nbt);
            this.oreName = oreName;
        }

        @Override
        public boolean matches(@Nonnull ItemStack stack)
        {
            return OreDictionary.containsMatch(this.nbt, OreDictionary.getOres(this.oreName), stack);
        }
    }
}
