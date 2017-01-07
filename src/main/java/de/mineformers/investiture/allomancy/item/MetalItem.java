package de.mineformers.investiture.allomancy.item;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.MetalMapping;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.allomancy.api.metal.stack.ModifiableMetalStack;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Callable;

public class MetalItem extends Item
{
    protected String name;
    protected String[] metalNames = {};
    protected Type type;

    /**
     * Creates a new instance of the ingot.
     */
    public MetalItem(String registryName, String name, Type type, String[] metalNames)
    {
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName(registryName);
        setUnlocalizedName(getRegistryName().toString());

        this.name = name;
        this.metalNames = metalNames;
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
        return MathHelper.clamp(value, 0, this.metalNames.length - 1);
    }

    /**
     * @param stack the stack to get the name from
     * @return the name of the metal represented by the given stack
     */
    public String getName(ItemStack stack)
    {
        return this.metalNames[clampDamage(stack.getItemDamage())];
    }

    public ModifiableMetalStack getMetalStack(ItemStack stack)
    {
        return (ModifiableMetalStack) Metals.getMetalStack(stack).get();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY,
                                      float hitZ)
    {
        player.sendMessage(new TextComponentString("Purity: " + getMetalStack(player.getHeldItem(hand)).getPurity()));
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        // Put purity in the tooltip
        MetalStack metal = getMetalStack(stack);
        tooltip.add(I18n.format("allomancy.message.purity", Investiture.proxy.getPercentageFormat().format(metal.getPurity())));
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        for (int dmg = 0; dmg < this.metalNames.length; dmg++)
        {
            // Add 50% pure metal to creative tab
            ItemStack impureStack = new ItemStack(item, 1, dmg);
            getMetalStack(impureStack).setPurity(0.5f);
            subItems.add(impureStack);

            // Add 100% pure metal to creative tab
            ItemStack pureStack = new ItemStack(item, 1, dmg);
            getMetalStack(pureStack).setPurity(1f);
            subItems.add(pureStack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return String.format("item.%s_%s", getName(stack), this.name);
    }

    public Type getItemType()
    {
        return this.type;
    }

    public void registerOreDict()
    {
        for (int i = 0; i < this.metalNames.length; i++)
        {
            String oreName = String.format("ingot%s", StringUtils.capitalize(this.metalNames[i]));
            ItemStack stack = new ItemStack(this, 1, i);
            MetalStack metal = Metals.getMetalStack(stack).get();
            OreDictionary.registerOre(oreName, stack);
            AllomancyAPIImpl.INSTANCE.registerMetalMapping(new MetalMapping.OreDict(oreName, metal.getMetal(), type.conversion, 1f, false));
        }
    }

    public String[] getMetalNames()
    {
        return this.metalNames;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        return new CapabilityProvider(stack);
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

    private class CapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
    {
        private final ItemStack stack;
        private final Impl instance;

        private CapabilityProvider(ItemStack stack)
        {
            this.stack = stack;
            this.instance = new Impl();
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
        {
            return capability == Capabilities.METAL_STACK;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            return instance.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            instance.deserializeNBT(nbt);
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
        {
            if (capability == Capabilities.METAL_STACK)
            {
                return Capabilities.METAL_STACK.cast(instance);
            }
            return null;
        }

        private class Impl implements ModifiableMetalStack, INBTSerializable<NBTTagCompound>
        {
            private float purity = 0;

            @Override
            public Metal getMetal()
            {
                return Metals.get(getName(stack));
            }

            @Override
            public float getQuantity()
            {
                return getItemType().conversion * stack.getCount();
            }

            @Override
            public float getPurity()
            {
                return purity;
            }

            @Override
            public void setPurity(float purity)
            {
                this.purity = MathHelper.clamp(purity, 0f, 1f);
            }

            @Override
            public NBTTagCompound serializeNBT()
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setFloat("Purity", purity);
                return tag;
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt)
            {
                this.purity = nbt.getFloat("Purity");
            }
        }
    }
}
