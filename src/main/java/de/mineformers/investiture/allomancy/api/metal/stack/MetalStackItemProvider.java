package de.mineformers.investiture.allomancy.api.metal.stack;

import com.google.common.collect.Range;
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
    private final Impl instance;

    public MetalStackItemProvider(ItemStack stack, Metal metal, float conversionRate, float basePurity, Range<Float> purityRange)
    {
        this.instance = new Impl(stack, new MetalStack(metal, conversionRate, basePurity), purityRange);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == Capabilities.METAL_STACK_PROVIDER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == Capabilities.METAL_STACK_PROVIDER)
        {
            return Capabilities.METAL_STACK_PROVIDER.cast(instance);
        }
        return null;
    }

    public static class Impl extends SingleMetalStackProvider
    {
        private final MetalStack metalStack;
        private final Range<Float> purityRange;

        public Impl(ItemStack stack, MetalStack metalStack, Range<Float> purityRange)
        {
            super(stack, metalStack.getPurity());
            this.metalStack = metalStack;
            this.purityRange = purityRange;
        }

        @Override
        public MetalStack baseStack()
        {
            return metalStack;
        }

        public float lowerPurityBound()
        {
            return purityRange.hasLowerBound() ? purityRange.lowerEndpoint() : 0;
        }

        public float upperPurityBound()
        {
            return purityRange.hasUpperBound() ? purityRange.upperEndpoint() : 1;
        }
    }
}
