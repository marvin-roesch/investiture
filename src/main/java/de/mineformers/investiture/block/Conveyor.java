package de.mineformers.investiture.block;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.tileentity.ConveyorInterface;
import de.mineformers.investiture.util.Vectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class Conveyor extends Block
{
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<InterfaceType> INTERFACE_TYPE = PropertyEnum.create("interface_type", InterfaceType.class);
    public static final PropertyBool CONNECTED_FRONT = PropertyBool.create("connected_front");
    public static final PropertyBool CONNECTED_BACK = PropertyBool.create("connected_back");
    public static final PropertyBool CONNECTED_LEFT = PropertyBool.create("connected_left");
    public static final PropertyBool CONNECTED_RIGHT = PropertyBool.create("connected_right");

    public Conveyor()
    {
        super(Material.IRON);
        setRegistryName(Investiture.MOD_ID, "conveyor");
        setUnlocalizedName(Investiture.MOD_ID + ".conveyor");
        setHardness(5);
        setResistance(10);
        setSoundType(SoundType.METAL);
        setDefaultState(this.blockState.getBaseState()
                                       .withProperty(FACING, EnumFacing.NORTH)
                                       .withProperty(INTERFACE_TYPE, InterfaceType.NONE)
                                       .withProperty(CONNECTED_FRONT, false)
                                       .withProperty(CONNECTED_BACK, false)
                                       .withProperty(CONNECTED_LEFT, false)
                                       .withProperty(CONNECTED_RIGHT, false));
        setCreativeTab(Investiture.CREATIVE_TAB);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return new AxisAlignedBB(0, 0.6875, 0, 1, 1, 1);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> boxes, @Nullable Entity
        entity, boolean actualState)
    {
        addCollisionBoxToList(pos, mask, boxes, new AxisAlignedBB(0, 0.6875, 0, 1, 0.9375, 1));
//        AxisAlignedBB belt = new AxisAlignedBB(0.0625, 0.75, 0, 0.9375, 0.9375, 1);
//        AxisAlignedBB leftRim = new AxisAlignedBB(0, 0.6875, 0, 0.0625, 1, 1);
//        AxisAlignedBB rightRim = new AxisAlignedBB(0.9375, 0.6875, 0, 1, 1, 1);
//        Rotation rotation = Vectors.getRotation(state.getValue(FACING));
//        addCollisionBoxToList(pos, mask, boxes, Vectors.rotateBlockBoundsY(belt, rotation));
//        addCollisionBoxToList(pos, mask, boxes, Vectors.rotateBlockBoundsY(leftRim, rotation));
//        addCollisionBoxToList(pos, mask, boxes, Vectors.rotateBlockBoundsY(rightRim, rotation));
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        if (entity.posY < pos.getY() + 0.5)
            return;
        EnumFacing facing = state.getValue(FACING);
        InterfaceType type = state.getValue(INTERFACE_TYPE);
        double centerX = pos.getX() + 0.5 - entity.posX;
        double centerZ = pos.getZ() + 0.5 - entity.posZ;
        double delta = centerX * facing.getFrontOffsetX() + centerZ * facing.getFrontOffsetZ();
        if ((type == InterfaceType.INSERTER && delta < 0.25) || (type == InterfaceType.EXTRACTOR && delta > 0.25))
            return;
        double velocityX = 0.4 * 0.3 * 1.15 * facing.getFrontOffsetX() + centerX * 0.2 * Math.abs(facing.getFrontOffsetZ());
        double velocityZ = 0.4 * 0.3 * 1.15 * facing.getFrontOffsetZ() + centerZ * 0.2 * Math.abs(facing.getFrontOffsetX());
        entity.motionX = velocityX;
        entity.motionZ = velocityZ;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                   .withProperty(INTERFACE_TYPE, meta == 1 ? InterfaceType.INSERTER : InterfaceType.NONE);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
        this.setDefaultFacing(world, pos, state);
    }

    private void setDefaultFacing(World world, BlockPos pos, IBlockState state)
    {
        if (!world.isRemote)
        {
            IBlockState north = world.getBlockState(pos.north());
            IBlockState south = world.getBlockState(pos.south());
            IBlockState west = world.getBlockState(pos.west());
            IBlockState east = world.getBlockState(pos.east());
            EnumFacing facing = state.getValue(FACING);

            if (facing == EnumFacing.NORTH && north.isFullBlock() && !south.isFullBlock())
            {
                facing = EnumFacing.SOUTH;
            }
            else if (facing == EnumFacing.SOUTH && south.isFullBlock() && !north.isFullBlock())
            {
                facing = EnumFacing.NORTH;
            }
            else if (facing == EnumFacing.WEST && west.isFullBlock() && !east.isFullBlock())
            {
                facing = EnumFacing.EAST;
            }
            else if (facing == EnumFacing.EAST && east.isFullBlock() && !west.isFullBlock())
            {
                facing = EnumFacing.WEST;
            }

            world.setBlockState(pos, state.withProperty(FACING, facing), 2);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        EnumFacing facing = state.getValue(FACING);
        Rotation rotation = Vectors.getRotation(facing);
        IBlockState frontState = world.getBlockState(pos.offset(facing));
        IBlockState backState = world.getBlockState(pos.offset(facing.getOpposite()));
        IBlockState leftState = world.getBlockState(pos.offset(rotation.rotate(EnumFacing.WEST)));
        IBlockState rightState = world.getBlockState(pos.offset(rotation.rotate(EnumFacing.EAST)));
        boolean connectedLeft = leftState.getBlock() == this && leftState.getValue(FACING) == rotation.rotate(EnumFacing.EAST)
            && leftState.getValue(INTERFACE_TYPE) == InterfaceType.NONE;
        boolean connectedRight = rightState.getBlock() == this && rightState.getValue(FACING) == rotation.rotate(EnumFacing.WEST)
            && rightState.getValue(INTERFACE_TYPE) == InterfaceType.NONE;
        boolean connectedFront = (frontState.getBlock() == this && frontState.getValue(FACING) != facing.getOpposite())
            || (state.getValue(INTERFACE_TYPE) != InterfaceType.INSERTER
            && !frontState.getBlock().isAir(frontState, world, pos.offset(facing)))
            || connectedLeft || connectedRight;
        boolean connectedBack = (backState.getBlock() == this && backState.getValue(FACING) == facing
            && backState.getValue(INTERFACE_TYPE) != InterfaceType.INSERTER)
            || (state.getValue(INTERFACE_TYPE) != InterfaceType.EXTRACTOR
            && backState.getBlock() != this && !backState.getBlock().isAir(backState, world, pos.offset(facing.getOpposite())))
            || connectedLeft || connectedRight;
        return super.getActualState(state, world, pos)
                    .withProperty(CONNECTED_FRONT, connectedFront)
                    .withProperty(CONNECTED_BACK, connectedBack)
                    .withProperty(CONNECTED_LEFT, connectedLeft)
                    .withProperty(CONNECTED_RIGHT, connectedRight);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        InterfaceType type = state.getValue(INTERFACE_TYPE);
        if (type != InterfaceType.NONE)
        {
            if (!world.isRemote)
            {
                world.setBlockState(pos, state.withProperty(INTERFACE_TYPE, type == InterfaceType.INSERTER ? InterfaceType.EXTRACTOR
                                                                                                           : InterfaceType.INSERTER));
            }
            return true;
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(INTERFACE_TYPE) == InterfaceType.NONE ? 0 : 1;
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        int facingIndex = meta & 0b11;
        int interfaceType = (meta & 0b1100) >> 2;
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(facingIndex))
                                .withProperty(INTERFACE_TYPE, InterfaceType.values()[Math.min(interfaceType, 2)]);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(INTERFACE_TYPE).ordinal() << 2);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, INTERFACE_TYPE, CONNECTED_FRONT, CONNECTED_BACK, CONNECTED_LEFT, CONNECTED_RIGHT);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror)
    {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return state.getValue(INTERFACE_TYPE) != InterfaceType.NONE;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        if (state.getValue(INTERFACE_TYPE) != InterfaceType.NONE)
        {
            return new ConveyorInterface();
        }
        return null;
    }

    public enum InterfaceType implements IStringSerializable
    {
        NONE, INSERTER, EXTRACTOR;

        @Override
        public String getName()
        {
            return name().toLowerCase();
        }

        @Override
        public String toString()
        {
            return getName();
        }
    }

    public static class ItemRepresentation extends ItemBlock
    {
        public ItemRepresentation(Block block)
        {
            super(block);
            setHasSubtypes(true);
        }

        @Override
        public int getMetadata(int damage)
        {
            return damage;
        }

        @Override
        public String getUnlocalizedName(ItemStack stack)
        {
            return String.format("tile.investiture.conveyor.%s",
                                 stack.getItemDamage() == 1 ? "interface" : "normal");
        }
    }
}
