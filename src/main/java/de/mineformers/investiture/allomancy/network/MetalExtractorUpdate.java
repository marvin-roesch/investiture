package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.network.TileEntityUpdate;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * Updates a metal extractor tile entity
 */
public class MetalExtractorUpdate extends TileEntityUpdate
{
    public boolean validMultiBlock;
    public EnumFacing orientation;

    public MetalExtractorUpdate()
    {
        super();
    }

    public MetalExtractorUpdate(BlockPos pos, boolean validMultiBlock, EnumFacing orientation)
    {
        super(pos);
        this.validMultiBlock = validMultiBlock;
        this.orientation = orientation;
    }
}
