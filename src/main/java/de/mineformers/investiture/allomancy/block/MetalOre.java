package de.mineformers.investiture.allomancy.block;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.block.properties.PropertyString;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

/**
 * Used as the ore for all allomantic metals which are not alloys and can be collected directly through mining.
 */
public class MetalOre extends Block
{
    public static final String[] NAMES = {"copper", "zinc", "tin", "aluminium", "chromium", "silver", "bismuth", "lead", "nickel"};
    public static final PropertyString METAL = new PropertyString("metal", NAMES);

    /**
     * Clamps a given integer to the damage range of the block.
     *
     * @param value the value to clamp
     * @return the value, if it is contained by [0..4], 0, if the value is lower than 0, or 4, if the value is greater than 4
     */
    public static int clampDamage(int value)
    {
        return MathHelper.clamp_int(value, 0, NAMES.length - 1);
    }

    /**
     * Creates a new instance of the ore.
     */
    public MetalOre()
    {
        super(Material.rock);

        setDefaultState(blockState.getBaseState().withProperty(METAL, NAMES[0]));
        setUnlocalizedName("allomantic_metal_ore");
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName("allomantic_metal_ore");
    }

    /**
     * Creates an {@link IBlockState block state} of the ore for the given metal.
     *
     * @param metal the metal to create the ore for
     * @return the block state of the ore
     */
    public IBlockState fromMetal(String metal)
    {
        return getDefaultState().withProperty(METAL, metal);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int dmg = 0; dmg < NAMES.length; dmg++)
        {
            list.add(new ItemStack(item, 1, dmg));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        // Required because of the layering of textures in the block model
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return getMetaFromState(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(METAL, NAMES[clampDamage(meta)]);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return Arrays.asList(NAMES).indexOf(state.getValue(METAL));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, METAL);
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
            return String.format("tile.%s_ore", NAMES[clampDamage(stack.getItemDamage())]);
        }
    }
}
