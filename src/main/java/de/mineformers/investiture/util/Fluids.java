package de.mineformers.investiture.util;

import com.google.common.base.Throwables;
import de.mineformers.investiture.Investiture;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Provides utilities for working with fluids in block form or in tanks.
 */
public class Fluids
{
    private static final MethodHandle vanillaFlowVector;

    static
    {
        MethodHandle temp = null;
        try
        {
            Method method = ReflectionHelper.findMethod(BlockLiquid.class, null, new String[]{"getFlowVector", "func_180687_h"},
                                                        IBlockAccess.class, BlockPos.class);
            method.setAccessible(true);
            temp = MethodHandles.lookup().unreflect(method);
        }
        catch (ReflectiveOperationException e)
        {
            Throwables.propagate(e);
        }
        vanillaFlowVector = temp;
    }

    public static Vec3 getFlowVector(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockFluidBase)
            return ((BlockFluidBase) state.getBlock()).getFlowVector(world, pos);
        else if (state.getBlock() instanceof BlockLiquid)
            try
            {
                return (Vec3) vanillaFlowVector.bindTo(state.getBlock()).invoke(world, pos);
            }
            catch (Throwable throwable)
            {
                Investiture.log().error("Failed to get flow vector from vanilla block!", throwable);
            }
        return new Vec3(0, 0, 0);
    }
}
