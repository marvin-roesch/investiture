package de.mineformers.investiture.allomancy.tileentity;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.block.MetalExtractor.Part;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
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
 * Provides the logic of the metal extractor
 */
public class TileMetalExtractorMaster extends TileEntity
{
    private EnumFacing orientation = EnumFacing.NORTH;
    private boolean validMultiBlock = false;
    private ImmutableList<BlockPos> children = ImmutableList.of();

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList children = new NBTTagList();
        for (BlockPos child : this.children)
            children.appendTag(new NBTTagLong(child.toLong()));
        compound.setTag("Children", children);
        compound.setBoolean("ValidMultiBlock", validMultiBlock);
        compound.setInteger("Orientation", orientation.getHorizontalIndex());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        ImmutableList.Builder<BlockPos> childrenBuilder = ImmutableList.builder();
        NBTTagList children = compound.getTagList("Children", Constants.NBT.TAG_LONG);
        for (int i = 0; i < children.tagCount(); i++)
            childrenBuilder.add(BlockPos.fromLong(((NBTTagLong) children.get(i)).getLong()));
        this.children = childrenBuilder.build();
        this.validMultiBlock = compound.getBoolean("ValidMultiBlock");
        this.orientation = EnumFacing.getHorizontal(compound.getInteger("Orientation"));
    }

    private static final Part[][][] multiBlock = {
        {
            {FRAME, FRAME, FRAME, FRAME, FRAME},
            {FRAME, GLASS, CONTROLLER, GLASS, FRAME},
            {FRAME, GLASS, GLASS, GLASS, FRAME},
            {FRAME, FRAME, FRAME, FRAME, FRAME}
        },
        {
            {FRAME, FRAME, FRAME, FRAME, FRAME},
            {FRAME, GRINDER, GRINDER, GRINDER, FRAME},
            {FRAME, GRINDER, GRINDER, GRINDER, FRAME},
            {FRAME, FRAME, FRAME, FRAME, FRAME}
        },
        {
            {FRAME, FRAME, FRAME, FRAME, FRAME},
            {FRAME, GRINDER, GRINDER, GRINDER, FRAME},
            {FRAME, GRINDER, GRINDER, GRINDER, FRAME},
            {FRAME, FRAME, FRAME, FRAME, FRAME}
        },
        {
            {FRAME, FRAME, FRAME, FRAME, FRAME},
            {FRAME, GRINDER, GRINDER, GRINDER, FRAME},
            {FRAME, GRINDER, GRINDER, GRINDER, FRAME},
            {FRAME, FRAME, FRAME, FRAME, FRAME}
        },
        {
            {FRAME, FRAME, FRAME, FRAME, FRAME},
            {FRAME, GLASS, CONTROLLER, GLASS, FRAME},
            {FRAME, GLASS, GLASS, GLASS, FRAME},
            {FRAME, FRAME, FRAME, FRAME, FRAME}
        }
    };

    public boolean validateMultiBlock()
    {
        if (worldObj.isRemote)
            return false;
        Optional<EnumFacing> orientation = FluentIterable.from(Arrays.asList(EnumFacing.HORIZONTALS))
                                                         .firstMatch(f -> {
                                                             IBlockState state = worldObj.getBlockState(pos.offset(f));
                                                             return state.getBlock() == this.getBlockType() &&
                                                                 state.getValue(MetalExtractor.PART) == GRINDER;
                                                         });
        validMultiBlock = false;

        if (orientation.isPresent())
        {
            this.orientation = orientation.get();
            ImmutableList.Builder<BlockPos> childrenBuilder = ImmutableList.builder();
            EnumFacing horizontal = getHorizontal();
            BlockPos corner = pos.offset(horizontal.getOpposite(), 2).down();
            for (int depth = 0; depth < multiBlock.length; depth++)
            {
                Part[][] layer = multiBlock[depth];
                for (int y = 0; y < layer.length; y++)
                {
                    Part[] row = layer[y];
                    for (int width = 0; width < row.length; width++)
                    {
                        BlockPos childPos = corner.offset(horizontal, width).up(y).offset(orientation.get(), depth);
                        IBlockState state = worldObj.getBlockState(childPos);
                        if (state.getBlock() != this.getBlockType() && state.getValue(MetalExtractor.PART) != row[width])
                            return false;
                        if (!pos.equals(childPos))
                            childrenBuilder.add(childPos.subtract(pos));
                    }
                }
            }

            this.children = childrenBuilder.build();
            for (BlockPos child : children)
            {
                worldObj.setBlockState(pos.add(child), worldObj.getBlockState(pos.add(child)).withProperty(MetalExtractor.BUILT, true));
                ((TileMetalExtractorSlave) worldObj.getTileEntity(pos.add(child))).setMasterPosition(pos);
            }
            this.validMultiBlock = true;
            Investiture.net().sendToAllAround(new MetalExtractorUpdate(pos, true, orientation.get()),
                                              new NetworkRegistry.TargetPoint(worldObj.provider.getDimensionId(),
                                                                              pos.getX(), pos.getY(), pos.getZ(), 32));
        }
        return validMultiBlock;
    }

    public void invalidateMultiBlock()
    {
        if (!validMultiBlock)
            return;
        for (BlockPos child : children)
            if (worldObj.getBlockState(pos.add(child)).getBlock() == Allomancy.Blocks.metal_extractor)
                worldObj.setBlockState(pos.add(child), worldObj.getBlockState(pos.add(child))
                                                               .withProperty(MetalExtractor.BUILT, false)
                                                               .withProperty(MetalExtractor.MASTER, false));
        if (worldObj.getBlockState(pos).getBlock() == Allomancy.Blocks.metal_extractor)
            worldObj.setBlockState(pos, worldObj.getBlockState(pos)
                                                .withProperty(MetalExtractor.BUILT, false)
                                                .withProperty(MetalExtractor.MASTER, false));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        BlockPos min = pos.offset(getHorizontal().getOpposite(), 2).down();
        BlockPos max = pos.offset(getHorizontal(), 3).up(5).offset(orientation, 6);
        return new AxisAlignedBB(
            Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()),
            Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ())
        );
    }

    public boolean isValidMultiBlock()
    {
        return validMultiBlock;
    }

    public EnumFacing getOrientation()
    {
        return orientation;
    }

    public EnumFacing getHorizontal()
    {
        return orientation.getAxis() == EnumFacing.Axis.X ? EnumFacing.SOUTH : EnumFacing.EAST;
    }

    public void processUpdate(MetalExtractorUpdate update)
    {
        this.validMultiBlock = update.validMultiBlock;
        this.orientation = update.orientation;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return true;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return Investiture.net().getPacketFrom(new MetalExtractorUpdate(pos, validMultiBlock, orientation));
    }
}
