package de.mineformers.investiture.network;

import net.minecraft.util.math.BlockPos;

/**
 * Updates a tile entity somewhere in the world
 */
public class TileEntityUpdate extends Message
{
    public BlockPos pos;

    public TileEntityUpdate()
    {
        this.pos = BlockPos.ORIGIN;
    }

    public TileEntityUpdate(BlockPos pos)
    {
        this.pos = pos;
    }
}
