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
        VANILLA_FLOW_VECTOR = Reflection.methodHandle(BlockLiquid.class, Vec3d.class)
                                        .srgName("func_180687_h")
                                        .parameterType(IBlockAccess.class)
                                        .parameterType(BlockPos.class)
                                        .build();
    }

    /**
     * Calculates the flowing velocity of a fluid block, if present at a location.
     *
     * @param world the block's world
     * @param pos   the block's position
     * @return a vector indicating the fluid's velocity, a zero vector if there was no fluid at the position
     */
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

    /**
     * Calculates the effective flowing velocity from a set of 'flow points'.
     *
     * @param world       the points' world
     * @param translation a translation to apply to the points' position
     * @param points      the flow points
     * @return a vector indicating the effective fluid velocity at a point
     */
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

    /**
     * Represents a location to probe for a flow vector, providing a operation for combination with other data points.
     */
    public static class FlowPoint
    {
        /**
         * Creates a point with addition as the combination operation.
         *
         * @param pos the position of the point
         * @return an adding flow point
         */
        public static FlowPoint withAddition(BlockPos pos)
        {
            return new FlowPoint(pos, Vec3d::add);
        }

        /**
         * Creates a point with subtraction as the combination operation.
         *
         * @param pos the position of the point
         * @return a subtracting flow point
         */
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
