package de.mineformers.investiture.allomancy.block;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.extractor.ExtractorPart;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorSlave;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Provides blocks for all parts of the metal extractor multi block structure.
 * The metal extractor will convert ores into slightly purified metal chunks.
 */
public class MetalExtractor extends Block implements ExtractorPart
{
    public enum Part implements IStringSerializable
    {
        FRAME, GRINDER, GLASS, WHEEL;

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

    /**
     * Clamps a given integer to the damage range of the block.
     *
     * @param value the value to clamp
     * @return the value, if it is contained by [0..4], 0, if the value is lower than 0, or 4, if the value is greater than 4
     */
    public static int clampDamage(int value)
    {
        return MathHelper.clamp(value, 0, Part.values().length - 1);
    }

    /**
     * Creates a new instance of the ore.
     */
    public MetalExtractor()
    {
        super(Material.PISTON);
        setDefaultState(blockState.getBaseState()
                                  .withProperty(BUILT, false)
                                  .withProperty(PART, Part.FRAME));
        setUnlocalizedName("metal_extractor");
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName("metal_extractor");
    }

    @Override
    public float getBlockHardness(IBlockState state, World world, BlockPos pos)
    {
        if (state.getBlock() == this)
        {
            switch (state.getValue(PART))
            {
                case GLASS:
                    return 0.3f;
                default:
                    return 2;
            }
        }
        return super.getBlockHardness(state, world, pos);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this)
        {
            switch (state.getValue(PART))
            {
                case GLASS:
                    return 0;
                default:
                    return 2;
            }
        }
        return super.getExplosionResistance(world, pos, exploder, explosion);
    }

    /**
     * Creates an {@link IBlockState block state} of the extractor for the given part.
     *
     * @param part the variant to create the state for
     * @return the block state of the part
     */
    public IBlockState getPart(Part part)
    {
        return getDefaultState().withProperty(PART, part);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, NonNullList<ItemStack> list)
    {
        for (int dmg = 0; dmg < Part.values().length; dmg++)
        {
            list.add(new ItemStack(item, 1, dmg));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
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
        return new TileMetalExtractorSlave();
    }

    @Override
    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return 0;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        IBlockState oppositeState = world.getBlockState(pos.offset(side.getOpposite()));
        return !(oppositeState.getBlock() == this && state.getBlock() == this &&
            state.getValue(PART) == Part.GLASS && oppositeState.getValue(PART) == Part.GLASS) &&
            super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMetalExtractorSlave)
            ((TileMetalExtractorSlave) tile).getMaster().revalidateMultiBlock();
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        if (world.getBlockState(pos).getValue(PART) == Part.WHEEL)
        {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileMetalExtractorSlave)
                ((TileMetalExtractorSlave) tile).getMaster().revalidateMultiBlock();
        }
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
        boolean built = ((meta >> 2) & 0b1) == 1;
        int partIndex = meta & 0b11;
        return getDefaultState().withProperty(BUILT, built)
                                .withProperty(PART, Part.values()[partIndex]);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return (state.getValue(BUILT) ? 1 : 0) << 2
            | state.getValue(PART).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, PART, BUILT);
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
