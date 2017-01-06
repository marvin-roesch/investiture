package de.mineformers.investiture.allomancy.tileentity;

import com.sun.istack.internal.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Stores the multi-block's master position
 */
public class TileMetalExtractorSlave extends TileEntity
{
    private BlockPos master = BlockPos.ORIGIN;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);
        compound.setLong("MasterPosition", master.toLong());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.master = BlockPos.fromLong(compound.getLong("MasterPosition"));
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return true;
    }

    void setMasterPosition(BlockPos master)
    {
        this.master = master.subtract(pos);
    }

    public BlockPos getMasterPosition()
    {
        return pos.add(master);
    }

    @Nullable
    public TileMetalExtractorMaster getMaster()
    {
        TileEntity te = world.getTileEntity(getMasterPosition());
        if (te instanceof TileMetalExtractorMaster)
            return (TileMetalExtractorMaster) te;
        return null;
    }
}
