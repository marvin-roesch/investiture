package de.mineformers.investiture.allomancy.item;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.MetalHolder;
import de.mineformers.investiture.allomancy.api.metal.MetalMapping;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class MetalItem extends Item implements MetalHolder<ItemStack>
{

    protected String name;

    protected String[] names = {};

    protected Type type;

    /**
     * Creates a new instance of the ingot.
     */
    public MetalItem(String registryName, String name, Type type, String[] names)
    {
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName(registryName);
        setUnlocalizedName(getRegistryName().toString());

        this.name = name;
        this.names = names;
        this.type = type;
    }

    /**
     * Clamps a given integer to the damage range of the item.
     *
     * @param value the value to clamp
     * @return the value, if it is contained by [0..13], 0, if the value is lower than 0, or 13, if the value is greater than 4
     */
    public int clampDamage(int value)
    {
        return MathHelper.clamp(value, 0, this.names.length - 1);
    }

    /**
     * @param stack the stack to get the compound from
     * @return the NBT data held by the given stack
     */
    private NBTTagCompound getCompound(ItemStack stack)
    {
        if (stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
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
        tooltip.add(I18n.format("allomancy.message.purity", purity));
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems)
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

    public Metal getMetal(ItemStack stack)
    {
        Optional<Metal> metal = Metals.get(getName(stack));

        return metal.orElse(null);
    }

    public float getMetalQuantity(ItemStack stack)
    {
        return this.getItemType().conversion * stack.getCount();
    }

    public Type getItemType()
    {
        return this.type;
    }

    public void registerOreDict()
    {
        for (int i = 0; i < this.names.length; i++)
        {
            String oreName = String.format("ingot%s", StringUtils.capitalize(this.names[i]));
            ItemStack stack = new ItemStack(this, 1, i);

            OreDictionary.registerOre(oreName, stack);
            Metals.addMapping(new MetalMapping.MetalMappingOreDict(this.getMetal(stack), oreName, this.getMetalQuantity(stack), false));
        }
    }

    public String[] getNames()
    {
        return this.names;
    }

    public enum Type
    {
        NUGGET(1, () -> Allomancy.Items.NUGGET),
        BEAD(0.5F, () -> Allomancy.Items.BEAD),
        INGOT(9, () -> Allomancy.Items.INGOT),
        DUST(9, () -> Allomancy.Items.DUST),
        CHUNK(9, () -> Allomancy.Items.CHUNK);

        public final float conversion;
        public final Callable<MetalItem> getter;

        Type(float conversion, Callable<MetalItem> getter)
        {
            this.conversion = conversion;
            this.getter = getter;
        }

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }
}
