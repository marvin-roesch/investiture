package de.mineformers.investiture.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.List;

public interface ComplexBounds
{
    default List<AxisAlignedBB> getSelectionBoxes(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        return getCollisionBoxes(world, pos, state);
    }

    List<AxisAlignedBB> getCollisionBoxes(IBlockAccess world, BlockPos pos, IBlockState state);
}
