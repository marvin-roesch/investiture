package de.mineformers.allomancy.block;

import de.mineformers.allomancy.block.properties.PropertyString;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

/**
 * AllomanticMetalOre
 *
 * @author PaleoCrafter
 */
public class AllomanticMetalOre extends Block
{
    public static final String[] NAMES = {
        "copper", "zinc", "tin", "aluminium", "chromium"
    };
    public static final PropertyString METAL = new PropertyString("metal", NAMES);

    public static int clampDamage(int value)
    {
        return MathHelper.clamp_int(value, 0, NAMES.length - 1);
    }

    public AllomanticMetalOre()
    {
        super(Material.rock);
        setDefaultState(blockState.getBaseState().withProperty(METAL, NAMES[0]));
        setUnlocalizedName("allomantic_metal_ore");
        setCreativeTab(CreativeTabs.tabBlock);
        setRegistryName("allomantic_metal_ore");
    }

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
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
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
    protected BlockState createBlockState()
    {
        return new BlockState(this, METAL);
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
            return String.format("tile.%s_ore", NAMES[clampDamage(stack.getItemDamage())]);
        }
    }
}
