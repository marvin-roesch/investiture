package de.mineformers.investiture.allomancy.api.metal.stack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.MetalMapping;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class MetalStackMappingProvider implements ICapabilityProvider
{
    private final ItemStack stack;
    private final MetalMapping mapping;
    private final Impl instance;

    public MetalStackMappingProvider(ItemStack stack, MetalMapping mapping)
    {
        this.stack = stack;
        this.mapping = mapping;
        this.instance = new Impl();
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

    private class Impl extends SingleMetalStackProvider
    {
        private Impl()
        {
            super(stack, mapping.getPurity(stack));
        }

        @Override
        public float lowerPurityBound()
        {
            return mapping.getLowerPurityBound(stack);
        }

        @Override
        public float upperPurityBound()
        {
            return mapping.getUpperPurityBound(stack);
        }

        @Override
        public MetalStack baseStack()
        {
            return new MetalStack(mapping.getMetal(stack), mapping.getQuantity(stack) * stack.getCount(), mapping.getPurity(stack));
        }
    }
}
