package de.mineformers.investiture.allomancy.item;

import com.google.common.base.Optional;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.metal.AllomanticMetal;
import de.mineformers.investiture.allomancy.metal.AllomanticMetals;
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
abstract class AllomanticMetalItem extends Item
{

    protected String name;

    protected String[] names = {};

    /**
     * Creates a new instance of the ingot.
     */
    public AllomanticMetalItem(String registryName, String name, String[] names)
    {
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName(registryName);

        this.name = name;
        this.names = names;
    }

    /**
     * Clamps a given integer to the damage range of the item.
     *
     * @param value the value to clamp
     * @return the value, if it is contained by [0..13], 0, if the value is lower than 0, or 13, if the value is greater than 4
     */
    public int clampDamage(int value)
    {
        return MathHelper.clamp_int(value, 0, this.names.length - 1);
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
        return this.names[clampDamage(stack.getItemDamage())];
    }

    /**
     * @param stack the stack to get the name from
     * @return the purity of the metal represented by the given stack
     */
    public int getPurity(ItemStack stack)
    {
        return getCompound(stack).getInteger("purity");
    }

    public ItemStack setPurity(ItemStack stack, int purity)
    {
        getCompound(stack).setInteger("purity", purity);
        return stack;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        // Put purity in the tooltip
        int purity = getPurity(stack);
        tooltip.add(StatCollector.translateToLocalFormatted("allomancy.message.purity", purity));
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int dmg = 0; dmg < this.names.length; dmg++)
        {
            // Add 100% pure metal to creative tab
            ItemStack stack = new ItemStack(item, 1, dmg);
            getCompound(stack).setInteger("purity", 100);
            subItems.add(stack);

            // Add 50% pure metal to creative tab
            ItemStack impureStack = new ItemStack(item, 1, dmg);
            getCompound(impureStack).setInteger("purity", 50);
            subItems.add(impureStack);
        }
    }

    public String getUnlocalizedName(ItemStack stack)
    {
        return String.format("item.%s_%s", getName(stack), this.name);
    }

    public AllomanticMetal getMetal(ItemStack stack)
    {
        Optional<AllomanticMetal> metal = AllomanticMetals.get(getName(stack));

        if(metal.isPresent()) {
            return metal.get();
        }

        return null;
    }

    abstract public Type getItemType();

    public enum Type {
        NUGGET(1), BEAD(0.5F), INGOT(9), DUST(9), CHUNK(9);

        public final float conversion;

        Type(float conversion)
        {
            this.conversion = conversion;
        }
    }

}
