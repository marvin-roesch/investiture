package de.mineformers.investiture.api.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public abstract class MultiBlockTile<M extends MultiBlockTile.Master<M>> extends TileEntity
{
    private boolean initialised = false;
    private int part;
    private BlockPos masterPos;
    private IBlockState originalState;
    private EnumFacing orientation;
    private BlockPos localPos;
    private boolean invalid;

    public abstract MultiBlock multiBlock();

    @SuppressWarnings("unchecked")
    public M master()
    {
        M master = (M) world.getTileEntity(masterPos);
        if (master == null)
        {
            throw new IllegalStateException("Master of multiblock unexpectedly was null at position " + masterPos + "!");
        }
        return master;
    }

    @Nullable
    public BlockPos masterPos()
    {
        return masterPos;
    }

    public int part()
    {
        return part;
    }

    public EnumFacing orientation()
    {
        return orientation;
    }

    public IBlockState originalState()
    {
        return originalState;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean isMaster()
    {
        return master() == this;
    }

    public final void initialise(int part, BlockPos masterPos, IBlockState originalState, EnumFacing orientation)
    {
        if (initialised)
        {
            throw new IllegalStateException("Tried to re-initialise multiblock TE after initialisation!");
        }
        this.part = part;
        this.masterPos = masterPos;
        this.originalState = originalState;
        this.orientation = orientation;
        markDirty();
        initialised = true;
    }

    public final void revert()
    {
        invalid = true;
        if (originalState.getBlock() == Blocks.AIR)
        {
            return;
        }
        world.setBlockState(pos, originalState);
    }

    public World world()
    {
        return world;
    }

    public BlockPos pos()
    {
        return pos;
    }

    public BlockPos localPos()
    {
        if (localPos == null)
        {
            localPos = multiBlock().structure().getLocalPosition(part, orientation);
        }
        return localPos;
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        part = compound.getInteger("Part");
        NBTTagCompound master = compound.getCompoundTag("Master");
        masterPos = new BlockPos(master.getInteger("X"), master.getInteger("Y"), master.getInteger("Z"));
        orientation = EnumFacing.getFront(compound.getInteger("Orientation"));
        originalState = Block.getStateById(compound.getInteger("OriginalState"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound nbt = super.writeToNBT(compound);
        nbt.setInteger("Part", part);
        NBTTagCompound master = new NBTTagCompound();
        master.setInteger("X", masterPos.getX());
        master.setInteger("Y", masterPos.getY());
        master.setInteger("Z", masterPos.getZ());
        nbt.setTag("Master", master);
        nbt.setInteger("Orientation", orientation.getIndex());
        nbt.setInteger("OriginalState", Block.getStateId(originalState));
        return nbt;
    }

    public interface Master<M extends Master>
    {
        MultiBlock multiBlock();

        World world();

        BlockPos pos();

        BlockPos localPos();

        EnumFacing orientation();

        boolean validateMultiBlock();

        default void revertStructure(List<BlockPos> exceptions)
        {
            World world = world();
            multiBlock().structure().getPositions(pos().subtract(localPos()), orientation()).stream()
                        .filter(p -> !exceptions.contains(p))
                        .forEach(p ->
                                 {
                                     TileEntity tile = world.getTileEntity(p);
                                     if (tile instanceof MultiBlockTile<?>)
                                     {
                                         ((MultiBlockTile) tile).revert();
                                     }
                                 });
        }

        @SuppressWarnings("unchecked")
        default M master()
        {
            return (M) this;
        }
    }
}
