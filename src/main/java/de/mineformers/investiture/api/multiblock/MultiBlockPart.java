package de.mineformers.investiture.api.multiblock;

import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Provides access to a specific block within a multi block structure.
 */
public class MultiBlockPart extends BlockWorldState
{
    private final int partId;

    public MultiBlockPart(World world, BlockPos pos, boolean flag, int partId)
    {
        super(world, pos, flag);
        this.partId = partId;
    }

    /**
     * @return the local, sequential ID of the block within the multi block structure
     */
    public int index()
    {
        return partId;
    }
}
