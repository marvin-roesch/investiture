package de.mineformers.investiture.allomancy.api.metal.stack;

import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.MetalMapping;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
        @Override
        public Metal getMetal()
        {
            return mapping.getMetal(stack);
        }

        @Override
        public float getQuantity()
        {
            return mapping.getQuantity(stack);
        }

        @Override
        public float getPurity()
        {
            return mapping.getPurity(stack);
        }
    }
}
