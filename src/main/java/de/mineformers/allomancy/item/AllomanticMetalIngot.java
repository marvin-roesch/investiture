package de.mineformers.allomancy.item;

import de.mineformers.allomancy.Allomancy;
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
 * AllomanticMetalIngot
 *
 * @author PaleoCrafter
 */
public class AllomanticMetalIngot extends Item {
    public static final String[] NAMES = {
            "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel",
            "duralumin", "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
    };

    public static int clampDamage(int value) {
        return MathHelper.clamp_int(value, 0, NAMES.length - 1);
    }

    public AllomanticMetalIngot() {
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(Allomancy.CREATIVE_TAB);
        setRegistryName("allomantic_metal_ingot");
    }

    private NBTTagCompound getCompound(ItemStack stack) {
        if (stack.getTagCompound() == null)
            stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    public String getName(ItemStack stack) {
        return NAMES[clampDamage(stack.getItemDamage())];
    }

    public int getPurity(ItemStack stack) {
        return getCompound(stack).getInteger("purity");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        int purity = getPurity(stack);
        tooltip.add(StatCollector.translateToLocalFormatted("allomancy.message.purity", purity));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
        for (int dmg = 0; dmg < NAMES.length; dmg++) {
            ItemStack stack = new ItemStack(item, 1, dmg);
            getCompound(stack).setInteger("purity", 100);
            subItems.add(stack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return String.format("item.%s_ingot", getName(stack));
    }
}
