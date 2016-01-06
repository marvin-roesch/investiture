package de.mineformers.investiture.allomancy.item;

import de.mineformers.investiture.Investiture;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public class AllomanticMetalIngot extends Item
{
    public static final String[] NAMES = {
        "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel",
        "duralumin", "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
    };

    /**
     * Clamps a given integer to the damage range of the item.
     *
     * @param value the value to clamp
     * @return the value, if it is contained by [0..13], 0, if the value is lower than 0, or 13, if the value is greater than 4
     */
    public static int clampDamage(int value)
    {
        return MathHelper.clamp_int(value, 0, NAMES.length - 1);
    }

    /**
     * Creates a new instance of the ingot.
     */
    public AllomanticMetalIngot()
    {
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName("allomantic_metal_ingot");
    }

    /**
     * @param stack the stack to get the compound from
     * @return the NBT data held by the given stack
     */
    private NBTTagCompound getCompound(ItemStack stack)
    {
        if (stack.getTagCompound() == null)
            stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    /**
     * @param stack the stack to get the name from
     * @return the name of the metal represented by the given stack
     */
    public String getName(ItemStack stack)
    {
        return NAMES[clampDamage(stack.getItemDamage())];
    }

    /**
     * @param stack the stack to get the name from
     * @return the purity of the metal represented by the given stack
     */
    public int getPurity(ItemStack stack)
    {
        return getCompound(stack).getInteger("Purity");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        // Put purity in the tooltip
        int purity = getPurity(stack);
        tooltip.add(StatCollector.translateToLocalFormatted("allomancy.message.purity", purity));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int dmg = 0; dmg < NAMES.length; dmg++)
        {
            // Add 100% pure metal to creative tab
            ItemStack stack = new ItemStack(item, 1, dmg);
            getCompound(stack).setInteger("Purity", 100);
            subItems.add(stack);

            // Add 50% pure metal to creative tab
            ItemStack impureStack = new ItemStack(item, 1, dmg);
            getCompound(impureStack).setInteger("Purity", 50);
            subItems.add(impureStack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return String.format("item.%s_ingot", getName(stack));
    }
}
