package de.mineformers.investiture.allomancy.metal;

import de.mineformers.investiture.allomancy.item.MetalItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

public interface MetalMapping extends MetalItem
{

    boolean matches(@Nonnull ItemStack stack);

    class MetalMappingItem implements MetalMapping
    {

        protected final Metal metal;
        protected final ItemStack stack;
        protected final float quantity;
        protected final boolean nbt;

        MetalMappingItem(@Nonnull Metal metal, @Nonnull ItemStack stack, float quantity)
        {
            this(metal, stack, quantity, false);
        }

        MetalMappingItem(@Nonnull Metal metal, @Nonnull ItemStack stack, float quantity, boolean nbt)
        {
            this.metal = metal;
            this.stack = stack;
            this.quantity = quantity;
            this.nbt = nbt;
        }

        @Override
        public boolean matches(@Nonnull ItemStack stack)
        {
            if(this.nbt) {
                return ItemStack.areItemStacksEqual(stack, this.stack) && ItemStack.areItemStackTagsEqual(stack, this.stack);
            }

            return ItemStack.areItemStacksEqual(stack, this.stack);
        }

        @Override
        public Metal getMetal(@Nonnull ItemStack stack)
        {
            return this.metal;
        }

        @Override
        public float getMetalQuantity(@Nonnull ItemStack stack)
        {
            return this.quantity * stack.stackSize;
        }
    }

    class MetalMappingOreDict extends MetalMappingItem
    {

        protected final String oreName;

        MetalMappingOreDict(@Nonnull Metal metal, @Nonnull ItemStack stack, float quantity) {
            super(metal, stack, quantity);
            this.oreName = null;
        }

        MetalMappingOreDict(@Nonnull Metal metal, @Nonnull ItemStack stack, float quantity, boolean nbt) {
            super(metal, stack, quantity, nbt);
            this.oreName = null;
        }

        MetalMappingOreDict(@Nonnull Metal metal, String oreName, float quantity, boolean nbt) {
            super(metal, null, quantity, nbt);
            this.oreName = oreName;
        }

        @Override
        public boolean matches(@Nonnull ItemStack stack)
        {
            if(this.stack != null) {
                return OreDictionary.containsMatch(this.nbt, OreDictionary.getOres(this.oreName), stack);
            }

            return OreDictionary.itemMatches(this.stack, stack, this.nbt);
        }

    }

}
