package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.network.TileEntityUpdate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * Updates a metal extractor tile entity
 */
public class MetalExtractorUpdate extends TileEntityUpdate
{
    public boolean validMultiBlock;
    public EnumFacing orientation;
    public ItemStack processingInput;
    public int processingTimer;
    public float rotation;
    public float prevRotation;
    public double power;

    public MetalExtractorUpdate()
    {
        super();
    }

    public MetalExtractorUpdate(BlockPos pos, boolean validMultiBlock, EnumFacing orientation,
                                ItemStack processingInput, int processingTimer, float rotation, float prevRotation, double power)
    {
        super(pos);
        this.validMultiBlock = validMultiBlock;
        this.orientation = orientation;
        this.processingInput = processingInput;
        this.processingTimer = processingTimer;
        this.rotation = rotation;
        this.prevRotation = prevRotation;
        this.power = power;
    }
}
