package de.mineformers.investiture.allomancy.tileentity;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Delegates inventory interactions to master
 */
public class TileMetalExtractorOutput extends TileMetalExtractorSlave
{
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return getMaster().hasCapability(capability, facing) || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (getMaster().hasCapability(capability, facing))
        {
            return getMaster().getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }
}
