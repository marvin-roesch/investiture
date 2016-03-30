package de.mineformers.investiture.util;

import de.mineformers.investiture.Investiture;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Provides utilities for working with fluids in block form or in tanks.
 */
@ParametersAreNonnullByDefault
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

    public static Vec3d getFlowVector(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockFluidBase)
            return ((BlockFluidBase) state.getBlock()).getFlowVector(world, pos);
        else if (state.getBlock() instanceof BlockLiquid)
            try
            {
                return (Vec3d) VANILLA_FLOW_VECTOR.bindTo(state.getBlock()).invokeExact(world, pos);
            }
            catch (Throwable throwable)
            {
                Investiture.log().error("Failed to get flow vector from vanilla block!", throwable);
            }
        return new Vec3d(0, 0, 0);
    }

    public static Vec3d getFlowVector(IBlockAccess world, BlockPos translation, Collection<FlowPoint> points)
    {
        Vec3d result = new Vec3d(0, 0, 0);
        for (FlowPoint point : points)
        {
            Vec3d flowVector = getFlowVector(world, translation.add(point.pos));
            result = point.operation.apply(result, flowVector);
        }
        return result;
    }

    public static class FlowPoint
    {
        public static FlowPoint withAddition(BlockPos pos)
        {
            return new FlowPoint(pos, Vec3d::add);
        }

        public static FlowPoint withSubtraction(BlockPos pos)
        {
            return new FlowPoint(pos, Vec3d::subtract);
        }

        public final BlockPos pos;
        public final BiFunction<Vec3d, Vec3d, Vec3d> operation;

        public FlowPoint(BlockPos pos, BiFunction<Vec3d, Vec3d, Vec3d> operation)
        {
            this.pos = pos;
            this.operation = operation;
        }
    }
}
