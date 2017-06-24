package de.mineformers.investiture.api.multiblock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface MultiBlock
{
    MultiBlockStructure structure();

    IBlockState constructState(int part);

    boolean isTrigger(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing facing);
}
