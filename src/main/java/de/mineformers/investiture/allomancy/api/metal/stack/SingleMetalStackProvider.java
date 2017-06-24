package de.mineformers.investiture.allomancy.api.metal.stack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public abstract class SingleMetalStackProvider implements PurifiableMetalStackProvider, INBTSerializable<NBTTagCompound>
{
    private final ItemStack stack;
    private float purity;

    public SingleMetalStackProvider(ItemStack stack, float basePurity)
    {
        this.stack = stack;
        this.purity = basePurity;
    }

    public abstract MetalStack baseStack();

    public void setPurity(float purity)
    {
        this.purity = MathHelper.clamp(purity, lowerPurityBound(), upperPurityBound());
    }

    @Override
    public List<MetalStack> get()
    {
        MetalStack metalStack = baseStack();
        metalStack.setPurity(purity);
        return ImmutableList.of(metalStack.copy(metalStack.getQuantity() * stack.getCount(), metalStack.getPurity()));
    }

    @Override
    public List<MetalStack> consume(List<MetalStack> stacks, boolean simulate)
    {
        MetalStack metalStack = baseStack();
        metalStack.setPurity(purity);
        if (stacks.isEmpty() || stacks.get(0).getMetal() != metalStack.getMetal())
            return ImmutableList.of();
        float consumed = stacks.get(0).getQuantity();
        float conversion = metalStack.getQuantity();
        int completelyConsumed = MathHelper.floor(consumed / conversion);
        if (!simulate)
            stack.shrink(completelyConsumed);
        return ImmutableList.of(metalStack.copy(completelyConsumed * conversion, metalStack.getPurity()));
    }

    @Nonnull
    @Override
    public List<MetalStack> getStored(Metal metal)
    {
        MetalStack metalStack = baseStack();
        metalStack.setPurity(purity);
        if (metal == metalStack.getMetal())
            return get();
        return ImmutableList.of();
    }

    @Nonnull
    @Override
    public Set<Metal> getStoredMetals()
    {
        MetalStack metalStack = baseStack();
        metalStack.setPurity(purity);
        return ImmutableSet.of(metalStack.getMetal());
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
