package de.mineformers.investiture.util;

import de.mineformers.investiture.Investiture;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;

import java.lang.invoke.MethodHandle;

/**
 * Provides utilities for working with fluids in block form or in tanks.
 */
public class Fluids
{
    private static final MethodHandle VANILLA_FLOW_VECTOR;

    static
    {
        VANILLA_FLOW_VECTOR = Reflection.methodHandle(BlockLiquid.class)
                                        .mcpName("getFlowVector")
                                        .srgName("func_180687_h")
                                        .type(IBlockAccess.class)
                                        .type(BlockPos.class)
                                        .build();
    }

    public static Vec3 getFlowVector(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockFluidBase)
            return ((BlockFluidBase) state.getBlock()).getFlowVector(world, pos);
        else if (state.getBlock() instanceof BlockLiquid)
            try
            {
                return (Vec3) VANILLA_FLOW_VECTOR.bindTo(state.getBlock()).invokeExact(world, pos);
            }
            catch (Throwable throwable)
            {
                Investiture.log().error("Failed to get flow vector from vanilla block!", throwable);
            }
        return new Vec3(0, 0, 0);
    }
}
