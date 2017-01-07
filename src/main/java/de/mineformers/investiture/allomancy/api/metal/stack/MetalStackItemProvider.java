package de.mineformers.investiture.allomancy.api.metal.stack;

import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MetalStackItemProvider implements ICapabilityProvider
{
    private final ItemStack stack;
    private final Impl instance;

    public MetalStackItemProvider(ItemStack stack, Metal metal, float conversionRate, float purity)
    {
        this.stack = stack;
        this.instance = new Impl(metal, conversionRate, purity);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == Capabilities.METAL_STACK;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == Capabilities.METAL_STACK)
        {
            return Capabilities.METAL_STACK.cast(instance);
        }
        return null;
    }

    private class Impl implements MetalStack
    {
        private final Metal metal;
        private final float conversionRate;
        private final float purity;

        private Impl(Metal metal, float conversionRate, float purity)
        {
            this.metal = metal;
            this.conversionRate = conversionRate;
            this.purity = purity;
        }

        @Override
        public Metal getMetal()
        {
            return metal;
        }

        @Override
        public float getQuantity()
        {
            return conversionRate * stack.getCount();
        }

        @Override
        public float getPurity()
        {
            return purity;
        }
    }
}
