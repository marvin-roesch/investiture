package de.mineformers.investiture.multiblock;

import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Provides access to a specific block within a mulitblock structure.
 */
public class MultiBlockPart extends BlockWorldState
{
    private final int partId;

    public MultiBlockPart(World world, BlockPos pos, boolean flag, int partId)
    {
        super(world, pos, flag);
        this.partId = partId;
    }

    public int index()
    {
        return partId;
    }
}
