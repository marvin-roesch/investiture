package de.mineformers.investiture.allomancy.tileentity;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.block.MetalExtractor.Part;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Arrays;

import static de.mineformers.investiture.allomancy.block.MetalExtractor.Part.*;

/**
 * Stores the multi-block's master position
 */
public class TileMetalExtractorSlave extends TileEntity
{
    private BlockPos master = BlockPos.ORIGIN;

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setLong("MasterPosition", master.toLong());
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

    public BlockPos getMasterPosition() {
        return pos.add(master);
    }
}
