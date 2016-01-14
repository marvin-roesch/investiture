package de.mineformers.investiture.allomancy.block;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorDummy;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorOutput;
import de.mineformers.investiture.util.Inventories;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Provides blocks for all parts of the metal extractor multi block structure.
 * The metal extractor will convert ores into slightly purified metal chunks.
 */
public class MetalExtractor extends Block
{
    public enum Part implements IStringSerializable
    {
        FRAME, GRINDER, GLASS, CONTROLLER;

        @Override
        public String getName()
        {
            return this.name().toLowerCase();
        }

        @Override
        public String toString()
        {
            return getName();
        }
    }

    public static final PropertyEnum<Part> PART = PropertyEnum.create("part", Part.class);
    public static final PropertyBool BUILT = PropertyBool.create("built");
    public static final PropertyBool MASTER = PropertyBool.create("master");

    /**
     * Clamps a given integer to the damage range of the block.
     *
     * @param value the value to clamp
     *
     * @return the value, if it is contained by [0..4], 0, if the value is lower than 0, or 4, if the value is greater than 4
     */
    public static int clampDamage(int value)
    {
        return MathHelper.clamp_int(value, 0, Part.values().length - 1);
    }

    /**
     * Creates a new instance of the ore.
     */
    public MetalExtractor()
    {
        super(Material.piston);
        setDefaultState(blockState.getBaseState().withProperty(BUILT, false).withProperty(PART, Part.FRAME).withProperty(MASTER, false));
        setUnlocalizedName("metal_extractor");
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName("metal_extractor");
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote && state.getValue(PART) == Part.CONTROLLER) {
            if (!state.getValue(BUILT)) {
                world.setBlockState(pos, state.withProperty(BUILT, true).withProperty(MASTER, true));
                if (!((TileMetalExtractorMaster) world.getTileEntity(pos)).validateMultiBlock()) {
                    world.setBlockState(pos, state.withProperty(BUILT, false).withProperty(MASTER, false));
                }
            }
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
        if (state.getBlock() != this || tile == null) return;
        if (state.getValue(BUILT)) {
            switch (state.getValue(PART)) {
                case GLASS:
                    break;
                case CONTROLLER:
                    EnumFacing orientation = EnumFacing.NORTH;
                    if (state.getValue(MASTER)) {
                        orientation = ((TileMetalExtractorMaster) tile).getOrientation();
                    } else if (((TileMetalExtractorDummy) tile).getMaster() != null) {
                        orientation = ((TileMetalExtractorDummy) tile).getMaster().getOrientation();
                    }
                    float minDepth = 0.4375f;
                    float maxDepth = 0.5625f;
                    setBlockBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0, 0, orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0, orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 1, 1, orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 1);
                    break;
            }
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        if (!world.isRemote && entity instanceof EntityItem) {
            if (state.getValue(BUILT) && state.getValue(PART) == Part.CONTROLLER && state.getValue(MASTER)) {
                TileMetalExtractorMaster tile = (TileMetalExtractorMaster) world.getTileEntity(pos);
                ItemStack stack = ((EntityItem) entity).getEntityItem();
                if (Inventories.insert(tile, stack, TileMetalExtractorMaster.INPUT_SLOT, tile.getOrientation().getOpposite())) {
                    entity.setDead();
                }
            }
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
    {
        if (state.getValue(BUILT) && state.getValue(PART) == Part.CONTROLLER && state.getValue(MASTER)) {
            EnumFacing orientation = ((TileMetalExtractorMaster) world.getTileEntity(pos)).getOrientation();
            double minDepth = 0.4375;
            double maxDepth = 0.5625;
            AxisAlignedBB left = AxisAlignedBB.fromBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0, 0, orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0, orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 0.25, 1, orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 0.25);
            AxisAlignedBB middleBottom = AxisAlignedBB.fromBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0.25, 0.25, orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0.25, orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 0.75, 0.75, orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 0.75);
            AxisAlignedBB middleTop = AxisAlignedBB.fromBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0.25, 0.75, orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0.25, orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 0.75, 1, orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 0.75);
            AxisAlignedBB right = AxisAlignedBB.fromBounds(orientation.getAxis() == EnumFacing.Axis.X ? minDepth : 0.75, 0, orientation.getAxis() == EnumFacing.Axis.Z ? minDepth : 0.75, orientation.getAxis() == EnumFacing.Axis.X ? maxDepth : 1, 1, orientation.getAxis() == EnumFacing.Axis.Z ? maxDepth : 1);
            if (mask.intersectsWith(left)) list.add(left);
            if (mask.intersectsWith(middleBottom)) list.add(middleBottom);
            if (mask.intersectsWith(middleTop)) list.add(middleTop);
            if (mask.intersectsWith(right)) list.add(right);
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

    /**
     * Creates an {@link IBlockState block state} of the extractor for the given part.
     *
     * @param part the variant to create the state for
     *
     * @return the block state of the part
     */
    public IBlockState getPart(Part part)
    {
        return getDefaultState().withProperty(PART, part);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int dmg = 0; dmg < Part.values().length; dmg++) {
            list.add(new ItemStack(item, 1, dmg));
        }
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
        if (!hasTileEntity(state)) return null;
        if (state.getValue(MASTER)) {
            return new TileMetalExtractorMaster();
        } else if (state.getValue(PART) == Part.CONTROLLER && !state.getValue(MASTER)) {
            return new TileMetalExtractorOutput();
        } else {
            return new TileMetalExtractorDummy();
        }
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
    public int getLightOpacity(IBlockAccess world, BlockPos pos)
    {
        if (world.getBlockState(pos).getBlock() != this) return super.getLightOpacity(world, pos);
        return world.getBlockState(pos).getValue(PART) == Part.GLASS || world.getBlockState(pos).getValue(BUILT) ? 0 : 255;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMetalExtractorMaster) {
            ((TileMetalExtractorMaster) tile).invalidateMultiBlock();
        } else if (tile instanceof TileMetalExtractorDummy) {
            ((TileMetalExtractorDummy) tile).getMaster().invalidateMultiBlock();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(PART).ordinal();
    }

    @Override
    public boolean hasTileEntity()
    {
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        boolean master = ((meta >> 3) & 0b1) == 1;
        boolean built = ((meta >> 2) & 0b1) == 1;
        int partIndex = meta & 0b11;
        return getDefaultState().withProperty(MASTER, master).withProperty(BUILT, built).withProperty(PART, Part.values()[partIndex]);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return (state.getValue(MASTER) ? 1 : 0) << 3 | (state.getValue(BUILT) ? 1 : 0) << 2 | state.getValue(PART).ordinal();
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, PART, BUILT, MASTER);
    }

    /**
     * The item representation of this block, used for localisation and storing the block in inventories.
     */
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
            return String.format("tile.metal_extractor.%s", Part.values()[clampDamage(stack.getItemDamage())]);
        }
    }
}
