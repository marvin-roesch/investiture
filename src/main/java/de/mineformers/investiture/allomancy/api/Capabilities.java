package de.mineformers.investiture.allomancy.api;

import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities
{
    @CapabilityInject(Allomancer.class)
    public static Capability<Allomancer> ALLOMANCER;
    @CapabilityInject(MetalStack.class)
    public static Capability<MetalStack> METAL_STACK;
}
