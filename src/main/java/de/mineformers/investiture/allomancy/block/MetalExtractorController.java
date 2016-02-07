package de.mineformers.investiture.allomancy.block;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.api.AllomancyAPI;
import de.mineformers.investiture.allomancy.api.misting.Smoker;
import de.mineformers.investiture.allomancy.extractor.ExtractorPart;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorOutput;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorSlave;
import de.mineformers.investiture.inventory.Inventories;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Provides the block for the metal extractor controller.
 */
public class MetalExtractorController extends Block implements ExtractorPart
{
    public static final PropertyBool BUILT = PropertyBool.create("built");
    public static final PropertyBool MASTER = PropertyBool.create("master");

    /**
     * Creates a new instance of the ore.
     */
    public MetalExtractorController()
    {
        super(Material.piston);
        setDefaultState(blockState.getBaseState()
                                  .withProperty(BUILT, false)
                                  .withProperty(MASTER, false));
        setUnlocalizedName("metal_extractor.controller");
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName("metal_extractor_controller");
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            AllomancyAPIImpl.INSTANCE.toAllomancer(playerIn).get().grantPower(Smoker.class);
            playerIn.addChatComponentMessage(
                new ChatComponentText(AllomancyAPIImpl.INSTANCE.toAllomancer(playerIn).get().grantPower(Smoker.class).category()));
        }
        if (!world.isRemote && !state.getValue(BUILT))
        {
            world.setBlockState(pos, state.withProperty(BUILT, true).withProperty(MASTER, true));
            if (!((TileMetalExtractorMaster) world.getTileEntity(pos)).validateMultiBlock())
                world.setBlockState(pos, state.withProperty(BUILT, false).withProperty(MASTER, false));
            return true;
        }
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        TileEntity tile = world.getTileEntity(pos);
        setBlockBounds(0, 0, 0, 1, 1, 1);
        if (state.getBlock() != this || tile == null)
            return;
        if (state.getValue(BUILT))
        {
            EnumFacing orientation = EnumFacing.NORTH;
            if (state.getValue(MASTER))
                orientation = ((TileMetalExtractorMaster) tile).getOrientation();
            else if (((TileMetalExtractorSlave) tile).getMaster() != null)
                orientation = ((TileMetalExtractorSlave) tile).getMaster().getOrientation();
            float minDepth = 0.4375f;
            float maxDepth = 0.5625f;
            setBlockBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0, 0,
                           orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0,
                           orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 1, 1,
                           orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 1);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        if (!world.isRemote && entity instanceof EntityItem)
        {
            if (state.getValue(BUILT) && state.getValue(MASTER))
            {
                TileMetalExtractorMaster tile = (TileMetalExtractorMaster) world.getTileEntity(pos);
                ItemStack stack = ((EntityItem) entity).getEntityItem();
                if (Inventories.insert(tile, stack, TileMetalExtractorMaster.INPUT_SLOT, tile.getOrientation().getOpposite()))
                    entity.setDead();
            }
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask,
                                        List<AxisAlignedBB> list, Entity collidingEntity)
    {
        if (state.getValue(BUILT) && state.getValue(MASTER))
        {
            EnumFacing orientation = ((TileMetalExtractorMaster) world.getTileEntity(pos)).getOrientation();
            double minDepth = 0.4375;
            double maxDepth = 0.5625;
            AxisAlignedBB left = AxisAlignedBB.fromBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0, 0,
                                                          orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0,
                                                          orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 0.25, 1,
                                                          orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 0.25);
            AxisAlignedBB middleBottom = AxisAlignedBB.fromBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0.25, 0.25,
                                                                  orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0.25,
                                                                  orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 0.75, 0.75,
                                                                  orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 0.75);
            AxisAlignedBB middleTop = AxisAlignedBB.fromBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0.25, 0.75,
                                                               orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0.25,
                                                               orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 0.75, 1,
                                                               orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 0.75);
            AxisAlignedBB right = AxisAlignedBB.fromBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0.75, 0,
                                                           orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0.75,
                                                           orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 1, 1,
                                                           orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 1);
            if (mask.intersectsWith(left))
                list.add(left);
            if (mask.intersectsWith(middleBottom))
                list.add(middleBottom);
            if (mask.intersectsWith(middleTop))
                list.add(middleTop);
            if (mask.intersectsWith(right))
                list.add(right);
            return;
        }
        super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
    {
        setBlockBoundsBasedOnState(world, pos);
        return super.getCollisionBoundingBox(world, pos, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return state.getValue(BUILT);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        if (!hasTileEntity(state))
            return null;
        if (state.getValue(MASTER))
            return new TileMetalExtractorMaster();
        else
            return new TileMetalExtractorOutput();
    }

    @Override
    public boolean isFullBlock()
    {
        return false;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMetalExtractorMaster)
            ((TileMetalExtractorMaster) tile).invalidateMultiBlock();
        else if (tile instanceof TileMetalExtractorSlave)
            ((TileMetalExtractorSlave) tile).getMaster().invalidateMultiBlock();
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean hasTileEntity()
    {
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        boolean master = (meta >> 1 & 0b1) == 1;
        boolean built = (meta & 0b1) == 1;
        return getDefaultState().withProperty(MASTER, master)
                                .withProperty(BUILT, built);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return (state.getValue(MASTER) ? 1 : 0) << 1
            | (state.getValue(BUILT) ? 1 : 0);
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, BUILT, MASTER);
    }
}
