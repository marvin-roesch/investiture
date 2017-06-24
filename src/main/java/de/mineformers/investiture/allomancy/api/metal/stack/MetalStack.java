package de.mineformers.investiture.allomancy.api.metal.stack;

import com.google.common.base.Objects;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Capability to retrieve metal data.
 */
public final class MetalStack implements INBTSerializable<NBTTagCompound>
{
    private Metal metal;
    private float quantity;
    private float purity;

    public MetalStack(NBTTagCompound tag)
    {
        deserializeNBT(tag);
    }

    public MetalStack(Metal metal, float quantity, float purity)
    {
        this.metal = metal;
        this.quantity = quantity;
        this.purity = purity;
    }

    public Metal getMetal()
    {
        return metal;
    }

    public float getQuantity()
    {
        return quantity;
    }

    public void setQuantity(float quantity)
    {
        this.quantity = quantity;
    }

    public float getPurity()
    {
        return purity;
    }

    public void setPurity(float purity)
    {
        this.purity = purity;
    }

    public MetalStack copy()
    {
        return new MetalStack(metal, quantity, purity);
    }

    public MetalStack copy(float quantity, float purity)
    {
        return new MetalStack(metal, quantity, purity);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Metal", metal.id());
        tag.setFloat("Quantity", quantity);
        tag.setFloat("Purity", purity);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.metal = Metals.get(nbt.getString("Metal"));
        this.quantity = nbt.getFloat("Quantity");
        this.purity = nbt.getFloat("Purity");
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(metal, quantity, purity);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof MetalStack)
        {
            MetalStack stack = (MetalStack) obj;
            return stack.getMetal().equals(metal) && stack.getQuantity() == quantity && stack.getPurity() == purity;
        }
        return false;
    }
}
