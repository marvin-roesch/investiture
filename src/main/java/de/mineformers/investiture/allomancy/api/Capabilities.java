package de.mineformers.investiture.allomancy.api;

import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStackProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities
{
    @CapabilityInject(Allomancer.class)
    public static Capability<Allomancer> ALLOMANCER;
    @CapabilityInject(MetalStackProvider.class)
    public static Capability<MetalStackProvider> METAL_STACK_PROVIDER;
}
