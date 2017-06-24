package de.mineformers.investiture.allomancy.item;

import com.google.common.collect.Range;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.MetalMapping;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.allomancy.api.metal.stack.PurifiableMetalStackProvider;
import de.mineformers.investiture.allomancy.api.metal.stack.SingleMetalStackProvider;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class MetalItem extends Item
{
    protected String name;
    protected Metal[] metals = {};
    protected Type type;

    /**
     * Creates a new instance of the ingot.
     */
    public MetalItem(String registryName, String name, Type type, Metal[] metals)
    {
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(Investiture.CREATIVE_TAB);
        setRegistryName(registryName);
        setUnlocalizedName(getRegistryName().toString());

        this.name = name;
        this.metals = metals;
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
        return MathHelper.clamp(value, 0, this.metals.length - 1);
    }

    /**
     * @param stack the stack to get the name from
     * @return the metal represented by the given stack
     */
    public Metal getMetal(ItemStack stack)
    {
        return this.metals[clampDamage(stack.getItemDamage())];
    }

    public PurifiableMetalStackProvider getMetalStackProvider(ItemStack stack)
    {
        return (PurifiableMetalStackProvider) stack.getCapability(Capabilities.METAL_STACK_PROVIDER, null);
    }

    public MetalStack getMetalStack(ItemStack stack)
    {
        return Metals.getMetalStacks(stack).get(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (!isInCreativeTab(tab))
            return;
        for (int dmg = 0; dmg < this.metals.length; dmg++)
        {
            // Add 50% pure metal to creative tab
            ItemStack impureStack = new ItemStack(this, 1, dmg);
            PurifiableMetalStackProvider impureProvider = getMetalStackProvider(impureStack);
            impureProvider.setPurity(impureProvider.middlePurityBound());
            subItems.add(impureStack);

            // Add 100% pure metal to creative tab
            ItemStack pureStack = new ItemStack(this, 1, dmg);
            PurifiableMetalStackProvider pureProvider = getMetalStackProvider(pureStack);
            pureProvider.setPurity(impureProvider.upperPurityBound());
            subItems.add(pureStack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return String.format("item.%s_%s", getMetal(stack), this.name);
    }

    public Type getItemType()
    {
        return this.type;
    }

    public void registerOreDict()
    {
        for (int i = 0; i < this.metals.length; i++)
        {
            String oreName = String.format("%s%s", type.name().toLowerCase(), StringUtils.capitalize(this.metals[i].id()));
            ItemStack stack = new ItemStack(this, 1, i);
            MetalStack metal = getMetalStack(stack);
            OreDictionary.registerOre(oreName, stack);
            AllomancyAPIImpl.INSTANCE.registerMetalMapping(
                new MetalMapping.OreDict(oreName, metal.getMetal(),
                                         type.conversion, type.purityRange.hasLowerBound() ? type.purityRange.lowerEndpoint() : 0, type.purityRange,
                                         false));
        }
    }

    public Metal[] getMetals()
    {
        return this.metals;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        return new CapabilityProvider(stack);
    }

    public enum Type
    {
        NUGGET(1, Range.closed(0.5f, 1f), () -> Allomancy.Items.NUGGET),
        BEAD(0.5F, Range.closed(0.5f, 1f), () -> Allomancy.Items.BEAD),
        INGOT(9, Range.closed(0.5f, 1f), () -> Allomancy.Items.INGOT),
        DUST(9, Range.closed(0.5f, 1f), () -> Allomancy.Items.DUST),
        CHUNK(9, Range.closed(0f, 0.25f), () -> Allomancy.Items.CHUNK);

        public final float conversion;
        public final Range<Float> purityRange;
        public final Callable<MetalItem> getter;

        Type(float conversion, Range<Float> purityRange, Callable<MetalItem> getter)
        {
            this.conversion = conversion;
            this.purityRange = purityRange;
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
            return capability == Capabilities.METAL_STACK_PROVIDER;
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
            if (capability == Capabilities.METAL_STACK_PROVIDER)
            {
                return Capabilities.METAL_STACK_PROVIDER.cast(instance);
            }
            return null;
        }

        private class Impl extends SingleMetalStackProvider
        {
            private Impl()
            {
                super(stack, type.purityRange.hasLowerBound() ? type.purityRange.lowerEndpoint() : 0);
            }

            public float lowerPurityBound()
            {
                return type.purityRange.hasLowerBound() ? type.purityRange.lowerEndpoint() : 0;
            }

            public float upperPurityBound()
            {
                return type.purityRange.hasUpperBound() ? type.purityRange.upperEndpoint() : 1;
            }

            @Override
            public MetalStack baseStack()
            {
                return new MetalStack(getMetal(stack), getItemType().conversion, lowerPurityBound());
            }
        }
    }
}
