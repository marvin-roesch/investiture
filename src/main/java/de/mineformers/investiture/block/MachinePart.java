package de.mineformers.investiture.block;

import de.mineformers.investiture.Investiture;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Basic machine parts required across multiple modules.
 */
public class MachinePart extends Block
{
    public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

    public MachinePart()
    {
        super(Material.IRON);
        setRegistryName(Investiture.MOD_ID, "machine_part");
        setUnlocalizedName(Investiture.MOD_ID + ".machine_part");
        setHardness(5);
        setResistance(10);
        setSoundType(SoundType.METAL);
        setDefaultState(this.blockState.getBaseState().withProperty(TYPE, Type.FRAME));
        setCreativeTab(Investiture.CREATIVE_TAB);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        for (int i = 0; i < Type.values().length; i++)
        {
            list.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return getMetaFromState(state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        // Required because of the layering of textures in the block model
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(TYPE, Type.values()[MathHelper.clamp(meta, 0, Type.values().length - 1)]);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).ordinal();
    }

    @Override
    public BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE);
    }

    public enum Type implements IStringSerializable
    {
        FRAME, POWER_INPUT, POWER_OUTPUT, ITEM_INPUT, ITEM_OUTPUT, FLUID_INPUT, FLUID_OUTPUT;

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
            return String.format("tile.investiture.machine_part.%s",
                                 Type.values()[MathHelper.clamp(stack.getItemDamage(), 0, Type.values().length - 1)]);
        }
    }
}

